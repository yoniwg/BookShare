package com.hgyw.bookshare;

import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
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
        access = AccessManagerFactory.getInstance().getCustomerAccess();
        activity = (MainActivity) getActivity();
        if (getArguments() != null) {
            bookQuery = (BookQuery) getArguments().getSerializable(IntentsFactory.ARG_BOOK_QUERY);
        }
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
        ArrayAdapter<Book> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, bookList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Book book = adapter.getItem(position);

            Toast.makeText(activity, book.shortDescription(), Toast.LENGTH_SHORT).show();
            EntityActivity.startNewActivity(activity, book.getEntityType(), book.getId());
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_book_list, menu);
    }
}
