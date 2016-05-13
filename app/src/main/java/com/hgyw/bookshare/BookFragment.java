package com.hgyw.bookshare;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Supplier;
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

        GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
        Activity activity = getActivity();

        Book book = access.retrieve(Book.class, entityId);
        BookSummary bookSummary = access.getBookSummary(book);

        ObjectToViewAppliers.applyBook(activity.findViewById(R.id.bookContainer), book, bookSummary);
        ListView reviewListView = (ListView) activity.findViewById(R.id.reviewListView);

        List<BookReview> bookReviewList = access.findBookReviews(book);
        final int MAX_REVIEWS = 2;
        bookReviewList = bookReviewList.subList(0, Math.min(bookReviewList.size(), MAX_REVIEWS));
        reviewListView.setAdapter(new ApplyObjectAdapter<BookReview>(activity, R.layout.book_review_component, bookReviewList) {
            @Override
            protected void applyOnView(View view, int position) {
                BookReview review = getItem(position);
                Customer customer = access.retrieve(Customer.class, review.getCustomerId());
                ObjectToViewAppliers.applyBookReview(view, review, customer);
            }
        });

        LinearLayout bookMainLayout = (LinearLayout) activity.findViewById(R.id.bookFragmentLinearLayout);
        ListView supplierListView = (ListView) activity.findViewById(R.id.supplierListView); supplierListView.setVisibility(View.INVISIBLE);
        List<BookSupplier> bookSupplierList = access.findBookSuppliers(book);
        for (BookSupplier bookSupplier : bookSupplierList) {
            Supplier supplier = access.retrieve(Supplier.class, bookSupplier.getSupplierId());
            View view = activity.getLayoutInflater().inflate(R.layout.book_supplier_list_item, null);
            ObjectToViewAppliers.applyBookSupplier(view, bookSupplier, supplier);
            Button button = (Button) view.findViewById(R.id.button);
            button.setText(R.string.do_order);
            button.findViewById(R.id.button).setOnClickListener(v -> {
                Order order = new Order();
                order.setBookSupplierId(bookSupplier.getSupplierId());
                order.setAmount(1);
                order.setUnitPrice(bookSupplier.getPrice());
                access.getCart().addToCart(order);
                Toast.makeText(activity, activity.getString(R.string.order_added_to_cart), Toast.LENGTH_LONG).show();
            });
            bookMainLayout.addView(view);
        }
    }
}
