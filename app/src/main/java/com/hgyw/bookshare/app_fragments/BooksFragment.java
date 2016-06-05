package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;


public class BooksFragment extends ListFragment implements TitleFragment {

    private BookQuery bookQuery;
    private final GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        System.out.println("onAttach(Context)");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        System.out.println("onAttach(Activity)");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        bookQuery = getArguments() == null ? null : (BookQuery) getArguments().getSerializable(IntentsFactory.ARG_BOOK_QUERY);
        // TODO: Check what state of fragment is saved, and whether we are initializing again for false.
        new AsyncTask<Void, Book, List<Book>>() {
            @Override
            protected List<Book> doInBackground(Void... params) {
                List<Book> bookList = bookQuery == null ? access.findSpecialOffers(30) : access.findBooks(bookQuery);
                //for (Book book : bookList) publishProgress(book);
                return bookList;
            }

            @Override
            protected void onPostExecute(List<Book> bookList) {
                ApplyObjectAdapter<Book> adapter = new ListApplyObjectAdapter<Book>(getActivity(), R.layout.book_list_item, bookList) {
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
            }
        }.execute();

        setEmptyText(getString(R.string.no_items_list_view));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Book book = (Book) l.getItemAtPosition(position);
        startActivity(IntentsFactory.newEntityIntent(getActivity(), book));
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
        startActivity(IntentsFactory.editBookIntent(getActivity(), 0));
    }

    @Override
    public int getFragmentTitle() {
        return R.string.book_list_fragment_title;
    }
}
