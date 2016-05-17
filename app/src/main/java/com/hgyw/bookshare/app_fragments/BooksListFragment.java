package com.hgyw.bookshare.app_fragments;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.hgyw.bookshare.ApplyObjectAdapter;
import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;


public class BooksListFragment extends AbstractFragment<GeneralAccess> {

    private BookQuery bookQuery;

    public BooksListFragment() {
        super(R.layout.fragment_books_list, R.menu.menu_book_list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bookQuery = getArguments() == null ? null
                : (BookQuery) getArguments().getSerializable(IntentsFactory.ARG_BOOK_QUERY);

        ListView listView = (ListView) getActivity().findViewById(R.id.books_list_view);

        List<Book> bookList = bookQuery == null ? access.findSpecialOffers(30)
                : access.findBooks(bookQuery);

        ApplyObjectAdapter<Book> adapter = new ApplyObjectAdapter<Book>(getActivity(), R.layout.book_list_item, bookList) {
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
            startActivity(IntentsFactory.newEntityIntent(getActivity(), book));
        });
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
