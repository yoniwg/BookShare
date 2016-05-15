package com.hgyw.bookshare.app_fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hgyw.bookshare.ApplyObjectAdapter;
import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_activities.MainActivity;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;


public class BooksListFragment extends Fragment {

    private GeneralAccess access;
    private BookQuery bookQuery;
    private MainActivity activity;

    protected BooksListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        access = AccessManagerFactory.getInstance().getGeneralAccess();
        activity = (MainActivity) getActivity();
        bookQuery = getArguments() == null ? null : (BookQuery) getArguments().getSerializable(IntentsFactory.ARG_BOOK_QUERY);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_books_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = (ListView) activity.findViewById(R.id.books_list_view);

        List<Book> bookList = bookQuery == null ? access.findSpecialOffers(30) : access.findBooks(bookQuery);
        ApplyObjectAdapter<Book> adapter = new ApplyObjectAdapter<Book>(activity, R.layout.book_list_item, bookList) {
            @Override
            protected void applyOnView(View view, int position) {
                Book book = getItem(position);
                ObjectToViewAppliers.apply(view, book);
                BookSummary summary = access.getBookSummary(book);
                ObjectToViewAppliers.apply(view, summary);

            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Book book = adapter.getItem(position);
            startActivity(IntentsFactory.newEntityIntent(activity, book));
        });
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
