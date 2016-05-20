package com.hgyw.bookshare.app_fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Rating;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;

import java.util.List;

public class BookFragment extends EntityFragment implements BookReviewDialogFragment.BookReviewResultListener {


    private static final int RESULT_CODE_BOOK_REVIEW_DIALOG = 314346537;
    private RatingBar userRatingBar;
    private boolean isCustomer;
    private Book book;
    private BookReview userBookReview;

    public BookFragment() {
        super(R.layout.fragment_book, R.menu.menu_book, R.string.book_fragment_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        isCustomer = access.getUserType() == UserType.CUSTOMER;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set view of book details
        View bookContainer = view.findViewById(R.id.bookContainer);
        book = access.retrieve(Book.class, entityId);
        ObjectToViewAppliers.apply(bookContainer, book);
        BookSummary bookSummary = access.getBookSummary(book);
        ObjectToViewAppliers.apply(bookContainer, bookSummary);

        // set view of user review
        View userReviewContainer = view.findViewById(R.id.userReviewContainer);
        if (!isCustomer) {
            userReviewContainer.setVisibility(View.GONE);
        } else {
            userRatingBar = (RatingBar) userReviewContainer.findViewById(R.id.userRatingBar);
            CustomerAccess cAccess = (CustomerAccess) access;

            userBookReview = cAccess.retrieveMyReview(book);
            if (userBookReview == null) {
                userBookReview = new BookReview();
                userBookReview.setBookId(book.getId());
            }
            updateUserReviewView();
            userRatingBar.setOnRatingBarChangeListener((RatingBar ratingBar, float rating, boolean fromUser) -> {
                if (fromUser) {
                    BookReviewDialogFragment dialogFragment = BookReviewDialogFragment.newInstance(userBookReview, rating);
                    dialogFragment.show(getFragmentManager(), "BookReviewDialog");
                }
            });
        }

        // set views of reviews
        List<BookReview> bookReviewList = access.findBookReviews(book);
        final int MAX_REVIEWS = 2;
        if (isCustomer) {bookReviewList.remove(userBookReview);}
        Button allReviewsButton = (Button) view.findViewById(R.id.all_reviews_button);
        if (bookReviewList.size() <= MAX_REVIEWS) { allReviewsButton.setVisibility(View.GONE); }
        bookReviewList = bookReviewList.subList(0, Math.min(bookReviewList.size(), MAX_REVIEWS));
        ListView reviewListView = (ListView) view.findViewById(R.id.reviewListView);
        reviewListView.setAdapter(new ApplyObjectAdapter<BookReview>(getActivity(), R.layout.book_review_component, bookReviewList) {
            @Override
            protected void applyOnView(View view, int position) {
                BookReview review = getItem(position);
                User customer = access.retrieve(User.class, review.getCustomerId());
                ObjectToViewAppliers.apply(view, review);
                ObjectToViewAppliers.apply(view, customer);
                TextView descriptionTextView = (TextView) view.findViewById(R.id.description);
                String description = review.getDescription();
                if (description.isEmpty()) descriptionTextView.setVisibility(View.GONE);

            }
        });

        // set views of suppliers
        LinearLayout bookMainLayout = (LinearLayout) view.findViewById(R.id.bookFragmentLinearLayout);
        List<BookSupplier> bookSupplierList = access.findBookSuppliers(book);
        for (BookSupplier bookSupplier : bookSupplierList) {
            View supplierView = createBookSupplierView(bookSupplier);
            bookMainLayout.addView(supplierView);
        }
    }

    @NonNull
    private View createBookSupplierView(BookSupplier bookSupplier) {
        User supplier = access.retrieve(User.class, bookSupplier.getSupplierId());
        View supplierView = getActivity().getLayoutInflater().inflate(R.layout.book_supplier_list_item, null);
        supplierView.setOnClickListener(v -> startActivity(IntentsFactory.newEntityIntent(getActivity(), supplier)));
        ObjectToViewAppliers.apply(supplierView, bookSupplier);
        ObjectToViewAppliers.apply(supplierView, supplier);
        Button buyButton = (Button) supplierView.findViewById(R.id.buy_button);
        if (!isCustomer) { buyButton.setVisibility(View.GONE); }
        buyButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.image_click_anim));
            AccessManagerFactory.getInstance().getCustomerAccess().addBookSupplierToCart(bookSupplier, 1);
            Toast.makeText(getActivity(), R.string.order_added_to_cart, Toast.LENGTH_SHORT).show();
        });
        return supplierView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_cart).setVisible(isCustomer);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cart:
                startActivity(IntentsFactory.newCartIntent(getActivity()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBookReviewResult(boolean canceled, BookReview bookReview, Rating oldUserRating) {
        if (canceled) {
            userRatingBar.setRating(oldUserRating.getStars());
        } else {
            // apply the customer details
            CustomerAccess cAccess = (CustomerAccess) access;
            cAccess.writeBookReview(bookReview);
            this.userBookReview = bookReview;
            updateUserReviewView();
            // message
            Toast.makeText(getActivity(), "The review was updated.", Toast.LENGTH_LONG).show();
        }

    }

    private void updateUserReviewView() {
        userRatingBar.setRating(userBookReview.getRating().getStars());
    }

}
