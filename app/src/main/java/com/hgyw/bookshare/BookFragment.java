package com.hgyw.bookshare;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.ArrayList;
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

        GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
        Activity activity = getActivity();
        Book book = access.retrieve(Book.class, entityId);

        TextView titleView = (TextView) activity.findViewById(R.id.bookTitleTextView);
        titleView.setText(book.getTitle());
        TextView authorView = (TextView) activity.findViewById(R.id.bookAuthorTextView);
        authorView.setText(book.getAuthor());
        TextView priceView = (TextView) activity.findViewById(R.id.bookPriceTextView);
        authorView.setText(book.getAuthor());
        ImageView imageView = (ImageView) activity.findViewById(R.id.bookImageView);
        Utility.setImageById(imageView, book.getImageId());
        RatingBar ratingBar = (RatingBar) activity.findViewById(R.id.bookRatingBar);

        BookSummary bookSummary = access.getBookSummary(book);
        ratingBar.setRating(bookSummary.clacMeanRating());
        TextView ratingTextView = (TextView) activity.findViewById(R.id.bookRatingTextView);
        ratingTextView.setText(Utility.moneyRangeToString(bookSummary.getMinPrice(), bookSummary.getMaxPrice()));

        ListView reviewsListView = (ListView) getActivity().findViewById(R.id.reviewListView);
        List<BookReview> bookReviewList = access.findBookReviews(book);
        bookReviewList = bookReviewList.subList(0, Math.min(bookReviewList.size(), 2));
        reviewsListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, bookReviewList));

        ListView supplierListView = (ListView) getActivity().findViewById(R.id.supplierListView);
        List<BookSupplier> bookSupplierList = access.findBookSuppliers(book);
        supplierListView.setAdapter(new BaseAdapter() {
            private Supplier suppliers[] = new Supplier[bookSupplierList.size()];

            @Override
            public int getCount() {
                return bookSupplierList.size();
            }

            @Override
            public Object getItem(int position) {
                return bookSupplierList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                BeautifulListItemView view;
                Stream.of(3,4,56,3).collect(Collectors.toCollection(ArrayList::new));
                if (convertView == null) view = new BeautifulListItemView(activity);
                else view = (BeautifulListItemView) convertView;

                BookSupplier bookSupplier = bookSupplierList.get(position);
                Supplier supplier;
                if ((supplier = suppliers[position]) == null) {
                    supplier = suppliers[position] = access.retrieve(Supplier.class, bookSupplier.getSupplierId());
                }
                view.getTitleView().setText(supplier.getFirstName() + " " + supplier.getLastName());
                view.getDescriptionView().setText(supplier.getAddress());
                view.getMoreTextView().setText(Utility.moneyToString(bookSupplier.getPrice()));
                Utility.setImageById(view.getThumbnailView(), supplier.getImageId());
                return view;
            }
        });


    }
}
