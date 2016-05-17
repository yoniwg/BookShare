package com.hgyw.bookshare.app_fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hgyw.bookshare.ApplyObjectAdapter;
import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_fragments.BookReviewDialogFragment;
import com.hgyw.bookshare.app_fragments.EntityFragment;
import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.Rating;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;

public class BookFragment extends EntityFragment {


    private static final int RESULT_CODE_BOOK_REVIEW_DIALOG = 314346537;
    private float oldUserRating;
    private RatingBar userRatingBar;
    private boolean isCustomer;

    public BookFragment() {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);


        isCustomer = AccessManagerFactory.getInstance().getCurrentUserType() == UserType.CUSTOMER;
        Activity activity = getActivity();

        View bookContainer = activity.findViewById(R.id.bookContainer);
        Book book = access.retrieve(Book.class, entityId);
        ObjectToViewAppliers.apply(bookContainer, book);
        BookSummary bookSummary = access.getBookSummary(book);
        ObjectToViewAppliers.apply(bookContainer, bookSummary);


        View userReviewContainer = activity.findViewById(R.id.userReviewContainer);
        BookReview userBookReview = null;
        if (!isCustomer) {
            userReviewContainer.setVisibility(View.GONE);
        } else {
            userRatingBar = (RatingBar) userReviewContainer.findViewById(R.id.userRatingBar);
            userBookReview = AccessManagerFactory.getInstance().getCustomerAccess().retrieveMyReview(book);
            final BookReview finalUserBookReview = userBookReview == null ? new BookReview() : userBookReview;
            finalUserBookReview.setBookId(book.getId());
            userRatingBar.setRating(finalUserBookReview.getRating().getStars());
            oldUserRating = userRatingBar.getRating();
            userRatingBar.setOnRatingBarChangeListener((RatingBar ratingBar, float rating, boolean fromUser) -> {
                if (fromUser) {
                    finalUserBookReview.setRating(Rating.ofStars((int) rating));
                    BookReviewDialogFragment dialogFragment = BookReviewDialogFragment.newInstance(finalUserBookReview);
                    dialogFragment.setTargetFragment(this, RESULT_CODE_BOOK_REVIEW_DIALOG); // TODO - Problem with targetFragment - we can do simple dialog without fragment
                    dialogFragment.show(getFragmentManager(), "BookReviewDialog");
                }
            });
        }
        List<BookReview> bookReviewList = access.findBookReviews(book);
        final int MAX_REVIEWS = 2;
        if (isCustomer) {bookReviewList.remove(userBookReview);}
        Button allReviewsButton = (Button) activity.findViewById(R.id.all_reviews_button);
        if (bookReviewList.size() <= MAX_REVIEWS) { allReviewsButton.setVisibility(View.GONE); }
        bookReviewList = bookReviewList.subList(0, Math.min(bookReviewList.size(), MAX_REVIEWS));
        ListView reviewListView = (ListView) activity.findViewById(R.id.reviewListView);
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

        LinearLayout bookMainLayout = (LinearLayout) activity.findViewById(R.id.bookFragmentLinearLayout);
        ListView supplierListView = (ListView) activity.findViewById(R.id.supplierListView); supplierListView.setVisibility(View.INVISIBLE);
        List<BookSupplier> bookSupplierList = access.findBookSuppliers(book);
        for (BookSupplier bookSupplier : bookSupplierList) {
            Supplier supplier = access.retrieve(Supplier.class, bookSupplier.getSupplierId());
            View supplierView = activity.getLayoutInflater().inflate(R.layout.book_supplier_list_item, null);
            supplierView.setOnClickListener(v -> startActivity(IntentsFactory.newEntityIntent(activity, supplier)));
            ObjectToViewAppliers.apply(supplierView, bookSupplier);
            ObjectToViewAppliers.apply(supplierView, supplier);
            Button buyButton = (Button) supplierView.findViewById(R.id.buy_button);
            if (!isCustomer){buyButton.setVisibility(View.GONE);}
            buyButton.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.image_click_anim));
                AccessManagerFactory.getInstance().getCustomerAccess().addBookSupplierToCart(bookSupplier, 1);
                Toast.makeText(activity, activity.getString(R.string.order_added_to_cart), Toast.LENGTH_LONG).show();
            });
            bookMainLayout.addView(supplierView);
        }
    }

    @Override
    public int getTitleResource() {
        return R.string.book_fragment_title;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CODE_BOOK_REVIEW_DIALOG) {
            onBookReviewResult(
                    resultCode == BookReviewDialogFragment.CANCELED,
                    (BookReview) data.getSerializableExtra(BookReviewDialogFragment.ARG_RESULT_OBJECT)
            );
        }
    }

    private void onBookReviewResult(boolean canceled, BookReview bookReview) {
        // apply the customer details
        if (canceled) {
            userRatingBar.setRating(oldUserRating);
        } else {
            CustomerAccess access = AccessManagerFactory.getInstance().getCustomerAccess();
            access.writeBookReview(bookReview);
            oldUserRating = userRatingBar.getRating();
        }
        // message
        Toast.makeText(getActivity(), "The review was updated.", Toast.LENGTH_LONG).show();

    }

    @Override
    int getFragmentId() {
        return R.layout.fragment_book;
    }

    @Override
    int getMenuId() {
        return R.menu.menu_book;
    }
}
