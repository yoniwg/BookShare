package com.hgyw.bookshare;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;

public class BookFragment extends EntityFragment {


    public BookFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Book book = AccessManagerFactory.getInstance().getGeneralAccess().retrieve(Book.class, entityId);
        TextView bookText = (TextView) getActivity().findViewById(R.id.bookTextView);
        bookText.setText(book.toString());

        GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
        ListView supplierListView = (ListView) getActivity().findViewById(R.id.supplierListView);
        ListView reviewsListView = (ListView) getActivity().findViewById(R.id.reviewListView);
        List<BookSupplier> supplierList = access.findBookSuppliers(book);
        List<BookReview> bookReviewList = access.findBookReviews(book);
        supplierListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, supplierList));
        supplierListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, bookReviewList));
    }
}
