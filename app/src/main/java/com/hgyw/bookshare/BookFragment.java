package com.hgyw.bookshare;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.Rating;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;

public class BookFragment extends EntityFragment {


    private static final int RESULT_CODE_BOOK_REVIEW_DIALOG = 314346537;
    private float oldUserRating;
    private RatingBar userRatingBar;
    private boolean isCustomer;

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
        isCustomer = AccessManagerFactory.getInstance().getCurrentUserType() != UserType.CUSTOMER;
        Activity activity = getActivity();

        View bookContainer = activity.findViewById(R.id.bookContainer);
        Book book = access.retrieve(Book.class, entityId);
        ObjectToViewAppliers.apply(bookContainer, book);
        BookSummary bookSummary = access.getBookSummary(book);
        ObjectToViewAppliers.apply(bookContainer, bookSummary);

        List<BookReview> bookReviewList = access.findBookReviews(book);
        ListView reviewListView = (ListView) activity.findViewById(R.id.reviewListView);
        final int MAX_REVIEWS = 2;
        bookReviewList = bookReviewList.subList(0, Math.min(bookReviewList.size(), MAX_REVIEWS));
        reviewListView.setAdapter(new ApplyObjectAdapter<BookReview>(activity, R.layout.book_review_component, bookReviewList) {
            @Override
            protected void applyOnView(View view, int position) {
                BookReview review = getItem(position);
                Customer customer = access.retrieve(Customer.class, review.getCustomerId());
                ObjectToViewAppliers.apply(view, review);
                ObjectToViewAppliers.apply(view, customer);
                TextView descriptionTextView = (TextView) view.findViewById(R.id.description);
                String description = review.getDescription();
                if (description.isEmpty()) descriptionTextView.setVisibility(View.GONE);

            }
        });

        userRatingBar = (RatingBar) activity.findViewById(R.id.userRatingBar);
        if (isCustomer) {
            userRatingBar.setVisibility(View.GONE);
        } else {
            BookReview optionalUserBookReview = AccessManagerFactory.getInstance().getCustomerAccess().retrieveMyReview(book);
            final BookReview userBookReview = optionalUserBookReview == null ? new BookReview() : optionalUserBookReview;
            userRatingBar.setRating(userBookReview.getRating().getStars());
            oldUserRating = userRatingBar.getRating();
            userRatingBar.setOnRatingBarChangeListener((RatingBar ratingBar, float rating, boolean fromUser) -> {
                userBookReview.setBookId(book.getId());
                userBookReview.setRating(Rating.ofStars((int) rating));
                DialogFragment dialogFragment = BookReviewDialogFragment.newInstance(userBookReview);
                dialogFragment.setTargetFragment(this, RESULT_CODE_BOOK_REVIEW_DIALOG);
                dialogFragment.show(getFragmentManager(), "BookReviewDialog");
            });
        }

        LinearLayout bookMainLayout = (LinearLayout) activity.findViewById(R.id.bookFragmentLinearLayout);
        ListView supplierListView = (ListView) activity.findViewById(R.id.supplierListView); supplierListView.setVisibility(View.INVISIBLE);
        List<BookSupplier> bookSupplierList = access.findBookSuppliers(book);
        for (BookSupplier bookSupplier : bookSupplierList) {
            Supplier supplier = access.retrieve(Supplier.class, bookSupplier.getSupplierId());
            View supplierView = activity.getLayoutInflater().inflate(R.layout.book_supplier_list_item, null);
            supplierView.setOnClickListener(v -> startActivity(IntentsFactory.newEntityIntent(activity, supplier)));
            ObjectToViewAppliers.apply(supplierView, bookSupplier);
            ObjectToViewAppliers.apply(supplierView, supplier);
            Button button = (Button) supplierView.findViewById(R.id.buy_button);
            if (isCustomer){
                button.setVisibility(View.GONE);
            }
            button.setOnClickListener(v -> {
                Utility.addBookSupplierToCart(bookSupplier, 1);
                Toast.makeText(activity, activity.getString(R.string.order_added_to_cart), Toast.LENGTH_LONG).show();
            });
            bookMainLayout.addView(supplierView);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE_BOOK_REVIEW_DIALOG) {
            if (resultCode == BookReviewDialogFragment.CANCELED) {
                userRatingBar.setRating(oldUserRating);
            } else {
                oldUserRating = userRatingBar.getRating();
            }
        }
    }

    @Override
    public int getTitleResource() {
        return R.string.book_fragment_title;
    }
}
