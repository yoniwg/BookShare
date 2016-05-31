package com.hgyw.bookshare.app_fragments;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.hgyw.bookshare.app_drivers.ProgressDialogAsyncTask;
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


    private UserType userType;
    private Book book;
    private BookReview userBookReview;

    private RatingBar userRatingBar;
    private View view;
    private View bookContainer;
    private BookSummary bookSummary;

    public BookFragment() {
        super(R.layout.fragment_book, R.menu.menu_book, R.string.book_fragment_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        userType = access.getUserType();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        bookContainer = view.findViewById(R.id.bookContainer);
        final BookFragment fragment = this;

        // set view of book details
        updateBookDeatilsView();

        // set view of user review
        View userReviewContainer = view.findViewById(R.id.userReviewContainer);
        if (userType != UserType.CUSTOMER) {
            userReviewContainer.setVisibility(View.GONE);
        } else {
            userRatingBar = (RatingBar) userReviewContainer.findViewById(R.id.userRatingBar);
            CustomerAccess cAccess = (CustomerAccess) access;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    userBookReview = cAccess.retrieveMyReview(book);
                    return null;
                }
                @Override
                protected void onPostExecute(Void aVoid) {
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
            }.execute();

        }

        new AsyncTask<Void,Void,Void>() {
            List<BookReview> bookReviews;
            List<BookSupplier> suppliers;
            @Override
            protected Void doInBackground(Void... params) {
                // get lists
                bookReviews = access.findBookReviews(book);
                suppliers = access.findBookSuppliers(book);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                // apply reviews
                final int MAX_REVIEWS = 2;
                if (userType == UserType.CUSTOMER) {
                    bookReviews.remove(userBookReview);
                }
                boolean thereIsMoreReviews = bookReviews.size() > MAX_REVIEWS;
                bookReviews = bookReviews.subList(0, Math.min(bookReviews.size(), MAX_REVIEWS));
                // set reviews list
                LinearLayout reviewListView = (LinearLayout) view.findViewById(R.id.reviewListView);
                Button allReviewsButton = (Button) view.findViewById(R.id.all_reviews_button);
                Utility.addViewsByList(reviewListView, bookReviews, getActivity().getLayoutInflater(), R.layout.book_review_component, fragment::updateBookReviewView);
                if (!thereIsMoreReviews) {
                    allReviewsButton.setVisibility(View.GONE);
                } else {
                    allReviewsButton.setOnClickListener(v -> startActivity(IntentsFactory.allReviewsIntent(getActivity(), book)));
                }

                // set views of suppliers
                LinearLayout bookMainLayout = (LinearLayout) view.findViewById(R.id.bookFragmentLinearLayout);
                Utility.addViewsByList(bookMainLayout, suppliers, getActivity().getLayoutInflater(), R.layout.book_supplier_list_item, fragment::updateBookSupplierView);
            }
        }.execute();
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
        if (userType != UserType.CUSTOMER) { buyButton.setVisibility(View.GONE); }
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
                startBookSupplierDialogAsync();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startBookSupplierDialogAsync() {
        new ProgressDialogAsyncTask<Void,Void,BookSupplier>(getActivity()) {
            @Override
            protected BookSupplier doInBackground1(Void... params) {
                return AccessManagerFactory.getInstance().getSupplierAccess()
                        .retrieveMyBookSupplier(book)
                        .orElseGet(() -> {
                            BookSupplier bs = new BookSupplier();
                            bs.setBookId(entityId);
                            return bs;
                        });
            }
            @Override
            protected void onPostExecute1(BookSupplier bookSupplier) {
                BookSupplierDialogFragment.newInstance(bookSupplier)
                        .show(getFragmentManager(), "BookSupplierDialogFragment");
            }
        }.execute();
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
        ObjectToViewAppliers.apply(bookContainer, book);
        new AsyncTask<Void,Void,BookSummary>() {
            @Override
            protected BookSummary doInBackground(Void... params) {
                return bookSummary = access.getBookSummary(book);
            }
            @Override
            protected void onPostExecute(BookSummary summary) {
                ObjectToViewAppliers.apply(bookContainer, bookSummary);
            }
        }.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentsFactory.CODE_ENTITY_UPDATED && resultCode == Activity.RESULT_OK) {
           // updateBookDeatilsView(); //?????????????????? //TODO
        }
    }

    @Override
    public void onBookSupplierResult(ResultCode result, BookSupplier bookSupplier) {
        SupplierAccess sAccess = (SupplierAccess) access;
        switch (result) {
            case OK:
                new ProgressDialogAsyncTask<Void,Void,Void>(getActivity(), R.string.updating_book_supplying) {
                    @Override
                    protected Void doInBackground1(Void... params) {
                        if (bookSupplier.getId() == 0) {
                            sAccess.addBookSupplier(bookSupplier);
                            Toast.makeText(getActivity(), R.string.book_was_added_to_supplier, Toast.LENGTH_SHORT).show();
                        } else {
                            sAccess.updateBookSupplier(bookSupplier);
                            Toast.makeText(getActivity(), R.string.book_was_updated_to_supplier, Toast.LENGTH_SHORT).show();
                        }
                        return null;
                    }
                }.execute();
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
