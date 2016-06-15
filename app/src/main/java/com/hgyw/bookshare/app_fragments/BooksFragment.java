package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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


public class BooksFragment extends SwipeRefreshListFragment implements TitleFragment, SwipeRefreshLayout.OnRefreshListener {

    private BookQuery bookQuery;
    private final GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
    GoodAsyncListAdapter<Book> adapter;

    private Activity activity;

    @Override public void onAttach(Context context) {super.onAttach(context);this.activity = (Activity) context;}
    @Override public void onAttach(Activity activity) {super.onAttach(activity);this.activity = activity;}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        setEmptyText(getString(R.string.no_items_list_view));

        bookQuery = getArguments() == null ? null : (BookQuery) getArguments().getSerializable(IntentsFactory.ARG_BOOK_QUERY);

        adapter = new GoodAsyncListAdapter<Book>(activity, R.layout.book_list_item, this) {
            @Override
            public List<Book> retrieveList() {
                return bookQuery == null ? access.findSpecialOffers(30) : access.findBooks(bookQuery);
            }

            @Override
            public Object[] retrieveData(Book book) {
                BookSummary bookSummary = access.getBookSummary(book);
                ImageEntity bookImage = (book.getImageId() == 0) ?
                        null : access.retrieve(ImageEntity.class,book.getImageId());
                return new Object[] {bookSummary,bookImage} ;
            }

            @Override
            public void applyDataOnView(Book book, Object[] data, View view) {
                ObjectToViewAppliers.apply(view, book, false);
                ObjectToViewAppliers.apply(view, (BookSummary) data[0]);
                ObjectToViewAppliers.apply(view, (ImageEntity) data[1]);
            }
        };

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
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
}
