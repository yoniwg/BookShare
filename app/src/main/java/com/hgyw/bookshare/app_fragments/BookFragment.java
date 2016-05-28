package com.hgyw.bookshare.app_fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ObjectToViewUpdates;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Rating;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.SupplierAccess;

import java.util.List;
import java.util.NoSuchElementException;

public class BookFragment extends EntityFragment implements BookReviewDialogFragment.BookReviewResultListener, BookSupplierDialogFragment.ResultListener {


    private RatingBar userRatingBar;
    private boolean isCustomer;
    private Book book;
    private BookReview userBookReview;
    private View view;

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
        this.view = view;
        updateBookDeatilsView();

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

        // apply reviews
        List<BookReview> bookReviews = access.findBookReviews(book);
        final int MAX_REVIEWS = 2;
        if (isCustomer) {bookReviews.remove(userBookReview);}
        boolean thereIsMoreReviews = bookReviews.size() > MAX_REVIEWS;
        bookReviews = bookReviews.subList(0, Math.min(bookReviews.size(), MAX_REVIEWS));
        // set reviews list
        LinearLayout reviewListView = (LinearLayout) view.findViewById(R.id.reviewListView);
        Button allReviewsButton = (Button) view.findViewById(R.id.all_reviews_button);
        Utility.addViewsByList(reviewListView, bookReviews, getActivity().getLayoutInflater(), R.layout.book_review_component, this::updateBookReviewView);
        if (!thereIsMoreReviews) {
            allReviewsButton.setVisibility(View.GONE);
        } else {
            allReviewsButton.setOnClickListener(v -> startActivity(IntentsFactory.allReviewsIntent(getActivity(), book)));
        }

        // set views of suppliers
        LinearLayout bookMainLayout = (LinearLayout) view.findViewById(R.id.bookFragmentLinearLayout);
        List<BookSupplier> suppliers = access.findBookSuppliers(book);
        Utility.addViewsByList(bookMainLayout, suppliers, getActivity().getLayoutInflater(), R.layout.book_supplier_list_item, this::updateBookSupplierView);
    }

    private void updateBookReviewView(View reviewView, BookReview bookReview) {
        ObjectToViewUpdates.updateBookReviewView(access, reviewView, bookReview);
    }

    private void updateBookSupplierView(View supplierView, BookSupplier bookSupplier) {
        User supplier = access.retrieve(User.class, bookSupplier.getSupplierId());
        Intent intent = IntentsFactory.newEntityIntent(getActivity(), supplier);
        supplierView.setOnClickListener(v -> startActivityForResult(intent, IntentsFactory.CODE_ENTITY_UPDATED));
        ObjectToViewAppliers.apply(supplierView, bookSupplier);
        ObjectToViewAppliers.apply(supplierView, supplier);
        Button buyButton = (Button) supplierView.findViewById(R.id.buy_button);
        if (!isCustomer) { buyButton.setVisibility(View.GONE); }
        buyButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.image_click_anim));
            AccessManagerFactory.getInstance().getCustomerAccess().addBookSupplierToCart(bookSupplier, 1);
            Toast.makeText(getActivity(), R.string.order_added_to_cart, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_cart).setVisible(access.getUserType() == UserType.CUSTOMER);
        menu.findItem(R.id.action_edit_book).setVisible(access.getUserType() == UserType.SUPPLIER);
        menu.findItem(R.id.action_supply_book).setVisible(access.getUserType() == UserType.SUPPLIER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cart:
                startActivity(IntentsFactory.newCartIntent(getActivity()));
                return true;
            case R.id.action_edit_book:
                startActivity(IntentsFactory.editBookIntent(getActivity(), book.getId()));
                return true;
            case R.id.action_supply_book: {
                BookSupplier bs = AccessManagerFactory.getInstance().getSupplierAccess().retrieveMyBookSupplier(book);
                bs.setBookId(entityId);
                BookSupplierDialogFragment.newInstance(bs).show(getFragmentManager(), "BookSupplierDialogFragment");
                return true;
            }
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

    private void updateBookDeatilsView() {
        View bookContainer = view.findViewById(R.id.bookContainer);
        book = access.retrieve(Book.class, entityId);
        ObjectToViewAppliers.apply(bookContainer, book);
        BookSummary bookSummary = access.getBookSummary(book);
        ObjectToViewAppliers.apply(bookContainer, bookSummary);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentsFactory.CODE_ENTITY_UPDATED && resultCode == Activity.RESULT_OK) {
            updateBookDeatilsView(); //??????????????????
        }
    }

    @Override
    public void onBookSupplierResult(ResultCode result, BookSupplier bookSupplier) {
        SupplierAccess sAccess = (SupplierAccess) access;
        switch (result) {
            case OK:
                if (bookSupplier.getId() == 0) {
                    sAccess.addBookSupplier(bookSupplier);
                    Toast.makeText(getActivity(),R.string.book_was_added_to_supplier, Toast.LENGTH_SHORT).show();
                } else {
                    sAccess.updateBookSupplier(bookSupplier);
                    Toast.makeText(getActivity(),R.string.book_was_updated_to_supplier, Toast.LENGTH_SHORT).show();
                }
                startActivity(IntentsFactory.supplierBooksIntent(getActivity()));
                break;
            case CANCEL: break;
            case DELETE:
                try {
                    sAccess.removeBookSupplier(bookSupplier);
                    Toast.makeText(getActivity(),R.string.book_was_removed_to_supplier, Toast.LENGTH_SHORT).show();
                } catch (NoSuchElementException ignored) {}
                break;
        }
    }
}
