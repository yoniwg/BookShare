package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.GoodAsyncListAdapter;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.app_drivers.SwipeRefreshListFragment;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;
import java.util.logging.Filter;


public class BooksFragment extends SwipeRefreshListFragment implements SearchView.OnQueryTextListener, TitleFragment, SwipeRefreshLayout.OnRefreshListener {

    /**
     * Variable to store parent activity
     */
    private Activity activity;

    /**
     * Book Query which uses to filter the books list from data base.
     * Should be passed to data access.
     */
    private BookQuery bookQuery;

    /**
     * General logic access (pointer to singleton)
     */
    private final GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();

    /**
     * adapter of type {@link GoodAsyncListAdapter} for the books list
     */
    GoodAsyncListAdapter<Book> adapter;

    /**
     * Variable to store {@code SearchView MenuItem}
     */
    SearchView searchView;

    @Override public void onAttach(Context context) {super.onAttach(context);this.activity = (Activity) context;}
    @Override public void onAttach(Activity activity) {super.onAttach(activity);this.activity = activity;}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        setEmptyText(getString(R.string.no_items_list_view));

        //Retrieve arguments from intent
        bookQuery = getArguments() == null
                ? null
                : (BookQuery) getArguments().getSerializable(IntentsFactory.ARG_BOOK_QUERY);

        adapter = new GoodAsyncListAdapter<Book>(activity, R.layout.book_list_item, this) {
            @Override
            public List<Book> retrieveList() {
                return bookQuery == null ? access.findSpecialOffers(30) : access.findBooks(bookQuery);
            }

            @Override
            public Object[] retrieveData(Book book) {
                BookSummary bookSummary = access.getBookSummary(book);
                ImageEntity bookImage = access.retrieveOptional(ImageEntity.class,book.getImageId()).orElse(null);
                return new Object[] {bookSummary,bookImage} ;
            }

            @Override
            public void applyDataOnView(Book book, Object[] data, View view) {
                ObjectToViewAppliers.apply(view, book);
                ObjectToViewAppliers.apply(view, (BookSummary) data[0]);
                ObjectToViewAppliers.apply(view, (ImageEntity) data[1]);
            }
        };

        //set the adapter filter converter
        adapter.setFilterConverterFunction(Book::getTitle);

        //set refresh listener for swipe refresh
        setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        adapter.refreshRetrieveList();
        setRefreshing(false);
    }

    @Override
    public void onDestroy() {
        if (adapter != null) adapter.cancel();
        super.onDestroy();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Book book = adapter.getItem(position);
        startActivity(IntentsFactory.newEntityIntent(activity, book));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_book_list, menu);
        menu.findItem(R.id.action_add_book).setVisible(access.getUserType() == UserType.SUPPLIER);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                BookQueryDialogFragment.newInstance(bookQuery).show(getFragmentManager(), "BookQueryDialogFragment");
                return true;
            case R.id.action_add_book:
                startAddBookDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startAddBookDialog() {
        startActivity(IntentsFactory.editBookIntent(activity, 0));
    }

    @Override
    public int getFragmentTitle() {
        return R.string.book_list_fragment_title;
    }

    /**
     * Not in use method
     * @param query
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * On query changed listener for the {@code SearchView MenuItem}.
     * Passes the arguments to the adapter filter, and notify adapter for the changes.
     * @param newText
     * @return
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        adapter.notifyDataSetChanged();
        return false;
    }

}
