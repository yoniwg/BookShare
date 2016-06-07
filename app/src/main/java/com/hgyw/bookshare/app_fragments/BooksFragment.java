package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ListApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.app_drivers.SwipeRefreshListFragment;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class BooksFragment extends SwipeRefreshListFragment implements TitleFragment, SwipeRefreshLayout.OnRefreshListener {

    private BookQuery bookQuery;
    private final GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
    private Activity activity;
    List<Book> bookList = new ArrayList<Book>();
    ApplyObjectAdapter<Book> adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setEmptyText(getString(R.string.no_items_list_view));

        bookQuery = getArguments() == null ? null : (BookQuery) getArguments().getSerializable(IntentsFactory.ARG_BOOK_QUERY);

        initListViewWithAdapter();
        setOnRefreshListener(this);
    }

    /**
     * Initialize the list view with new adapter, and set onItemClick listener.
     */
    private void initListViewWithAdapter() {
        adapter = new ListApplyObjectAdapter<Book>(activity, R.layout.book_list_item, bookList) {
            @Override
            protected Object[] retrieveDataForView(Book book) {
                return new Object[] {access.getBookSummary(book)} ;
            }
            @Override
            protected void applyDataOnView(View view, Book book, Object[] items) {
                ObjectToViewAppliers.apply(view, book);
                ObjectToViewAppliers.apply(view, (BookSummary) items[0]);
                view.setVisibility(View.VISIBLE);
            }
        };
        setListAdapter(adapter);
        createRefreshingAsyncTask().execute();
    }

    @Override
    public void onRefresh() {
        AsyncTask refreshAsyncTask = createRefreshingAsyncTask().execute();
        try {
            refreshAsyncTask.get();
            setRefreshing(false);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    /**
     * Creating an AsyncTask which in charge of refreshing list data
     * and notifying adapter.
     * @return
     */
    private AsyncTask<Void,Book,List<Book>> createRefreshingAsyncTask(){
        return new AsyncTask<Void, Book, List<Book>>() {
            @Override
            protected List<Book> doInBackground(Void... params) {
                bookList.clear();
                bookList.addAll(bookQuery == null ? access.findSpecialOffers(30) : access.findBooks(bookQuery));
                //for (Book book : bookList) publishProgress(book);
                return bookList;
            }

            @Override
            protected void onPostExecute(List<Book> bookList) {
                adapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Book book = (Book)l.getItemAtPosition(position);
        startActivity(IntentsFactory.newEntityIntent(activity, book));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_book_list, menu);
        menu.findItem(R.id.action_add_book).setVisible(false);
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
