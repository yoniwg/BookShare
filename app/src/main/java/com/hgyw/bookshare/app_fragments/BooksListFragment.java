package com.hgyw.bookshare.app_fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;


public class BooksListFragment extends ListFragment implements TitleFragment {

    private BookQuery bookQuery;
    private final GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        bookQuery = getArguments() == null ? null
                : (BookQuery) getArguments().getSerializable(IntentsFactory.ARG_BOOK_QUERY);

        List<Book> bookList = bookQuery == null ? access.findSpecialOffers(30)
                : access.findBooks(bookQuery);

        ListAdapter adapter = new ApplyObjectAdapter<Book>(getActivity(), R.layout.book_list_item, bookList) {
            @Override
            protected void applyOnView(View view, int position) {
                Book book = getItem(position);
                ObjectToViewAppliers.apply(view, book);
                BookSummary summary = access.getBookSummary(book);
                ObjectToViewAppliers.apply(view, summary);
            }
        };
        setListAdapter(adapter);
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
