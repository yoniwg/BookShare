package com.hgyw.bookshare.app_fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.CancelableLoadingDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ObjectToViewUpdates;
import com.hgyw.bookshare.app_drivers.ProgressDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Rating;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.exceptions.OrdersTransactionException;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.SupplierAccess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookFragment extends EntityFragment implements BookReviewDialogFragment.BookReviewResultListener, BookSupplierDialogFragment.ResultListener {

    private UserType userType;
    private Book book;
    private BookReview userBookReview;
    private Optional<BookSupplier> oCurrentBookSupplier;
    private Map<BookReview, Pair<User,ImageEntity>> bookReviewsUserMap = new HashMap<>();
    private Map<BookSupplier, Pair<User,ImageEntity>> bookSuppliersUserMap = new HashMap<>();
    private BookSummary bookSummary;

    private RatingBar userRatingBar;
    private View bookContainer;
    private Activity activity;
    private Menu menu;
    private Map<BookSupplier, View> suppliersViewsMap;
    private LinearLayout suppliersListView;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        bookContainer = view.findViewById(R.id.bookContainer);
        final BookFragment fragment = this;

        View userReviewContainer = view.findViewById(R.id.userReviewContainer);
        LinearLayout reviewListView = (LinearLayout) view.findViewById(R.id.reviews_listview);
        suppliersListView = (LinearLayout) view.findViewById(R.id.suppliers_listview);
        Button allReviewsButton = (Button) view.findViewById(R.id.all_reviews_button);
        userRatingBar = (RatingBar) userReviewContainer.findViewById(R.id.userRatingBar);

        new CancelableLoadingDialogAsyncTask<Void,Void,Void>(activity) {
            ImageEntity bookImage;
            List<BookReview> bookReviews;
            List<BookSupplier> bookSuppliers;

            @Override
            protected Void retrieveDataAsync(Void... params) {
                // get book and user-review data
                book = access.retrieve(Book.class, entityId);
                bookSummary = access.getBookSummary(book);
                if (userType == UserType.CUSTOMER) userBookReview = ((CustomerAccess) access).retrieveMyReview(book);
                else oCurrentBookSupplier = ((SupplierAccess) access).retrieveMyBookSupplier(book);
                // get lists
                bookReviews = access.findBookReviews(book);
                bookSuppliers = access.findBookSuppliers(book);

                Stream.of(bookReviews).forEach(br->{
                        User u = access.retrieve(User.class, br.getCustomerId());
                        ImageEntity i = (u.getImageId() == 0) ? new ImageEntity() : access.retrieve(ImageEntity.class, u.getImageId());
                        bookReviewsUserMap.put(br, new Pair<>(u,i));
                        });
                Stream.of(bookSuppliers).forEach(bs->{
                        User u = access.retrieve(User.class, bs.getSupplierId());
                        ImageEntity i = (u.getImageId() == 0) ? new ImageEntity() : access.retrieve(ImageEntity.class, u.getImageId());
                        bookSuppliersUserMap.put(bs, new Pair<>(u,i));
                        });

                return null;
            }

            @Override
            protected void doByData(Void aVoid) {
                // set book details
                ObjectToViewAppliers.apply(bookContainer, book);
                ObjectToViewAppliers.apply(bookContainer, bookSummary);

                // set user review
                if (userType == UserType.CUSTOMER) {
                    if (userBookReview == null) {
                        userBookReview = new BookReview();
                        userBookReview.setBookId(book.getId());
                    }
                    userRatingBar.setRating(userBookReview.getRating().getStars());
                    userRatingBar.setOnRatingBarChangeListener((RatingBar ratingBar, float rating, boolean fromUser) -> {
                        if (fromUser) {
                            BookReviewDialogFragment dialogFragment = BookReviewDialogFragment.newInstance(userBookReview, rating);
                            dialogFragment.show(getFragmentManager(), "BookReviewDialog");
                        }
                    });
                } else {
                    menu.findItem(R.id.action_remove_book).setVisible(oCurrentBookSupplier.isPresent());
                    userReviewContainer.setVisibility(View.GONE);
                }

                // set reviews list to max-size
                final int MAX_REVIEWS = 2;
                if (userType == UserType.CUSTOMER) {
                    bookReviews.remove(userBookReview);
                }
                boolean thereIsMoreReviews = bookReviews.size() > MAX_REVIEWS;
                bookReviews = bookReviews.subList(0, Math.min(bookReviews.size(), MAX_REVIEWS));

                // set reviews list view
                Utility.addViewsByList(reviewListView, bookReviews, activity.getLayoutInflater(), R.layout.book_review_component, fragment::updateBookReviewView);
                if (!thereIsMoreReviews) {
                    allReviewsButton.setVisibility(View.GONE);
                } else {
                    allReviewsButton.setOnClickListener(v -> startActivity(IntentsFactory.allReviewsIntent(activity, book)));
                }

                // set views of suppliers
                suppliersViewsMap = Utility.addViewsByList(suppliersListView, bookSuppliers, activity.getLayoutInflater(), R.layout.book_supplier_list_item, fragment::updateBookSupplierView);
            }

            @Override
            protected void onCancel() {
                activity.finish();
            }
        }.execute();
    }

    private void updateBookReviewView(View reviewView, BookReview bookReview) {
        User customer = bookReviewsUserMap.get(bookReview).first;
        ImageEntity image = bookReviewsUserMap.get(bookReview).second;
        ObjectToViewUpdates.updateBookReviewView(reviewView, bookReview, customer, false);
        Utility.setImageByBytes((ImageView) reviewView.findViewById(R.id.userThumbnail), image.getBytes(), R.drawable.image_user);
    }

    private void updateBookSupplierView(View supplierView, BookSupplier bookSupplier) {
        User supplier = bookSuppliersUserMap.get(bookSupplier).first;
        ImageEntity image = bookSuppliersUserMap.get(bookSupplier).second;
        ObjectToViewUpdates.updateBookSupplierBuyView(supplierView, bookSupplier, supplier, false);
        Utility.setImageByBytes((ImageView) supplierView.findViewById(R.id.userThumbnail), image.getBytes(), R.drawable.image_user);
        Button buyButton = (Button) supplierView.findViewById(R.id.buy_button);
        //by default - invisible
        buyButton.setVisibility(View.GONE);

        if (userType == UserType.CUSTOMER) {
            buyButton.setVisibility(View.VISIBLE);
            setBuyButtonOnClick(buyButton,bookSupplier);
        }

        Intent intent = IntentsFactory.newEntityIntent(activity, supplier);
        supplierView.setOnClickListener(v -> startActivityForResult(intent, IntentsFactory.CODE_ENTITY_UPDATED));
    }

    private void setBuyButtonOnClick(Button buyButton,BookSupplier bookSupplier) {
        if (bookSupplier.getAmountAvailable() >= 1){
            buyButton.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.image_click_anim));
                try {
                    AccessManagerFactory.getInstance().getCustomerAccess().addBookSupplierToCart(bookSupplier, 1);
                    Toast.makeText(activity, R.string.order_added_to_cart, Toast.LENGTH_SHORT).show();
                } catch (OrdersTransactionException e) {
                    Toast.makeText(activity, R.string.order_not_enough_amount, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            buyButton.setAlpha(0.25f);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
        menu.findItem(R.id.action_cart).setVisible(access.getUserType() == UserType.CUSTOMER);
        menu.findItem(R.id.action_edit_book).setVisible(access.getUserType() == UserType.SUPPLIER);
        menu.findItem(R.id.action_supply_book).setVisible(access.getUserType() == UserType.SUPPLIER);
        menu.findItem(R.id.action_remove_book).setVisible(access.getUserType() == UserType.SUPPLIER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cart:
                startActivity(IntentsFactory.newCartIntent(activity));
                return true;
            case R.id.action_edit_book:
                startActivity(IntentsFactory.editBookIntent(activity, book.getId()));
                return true;
            case R.id.action_remove_book:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.remove_from_my_books_message)
                        .setPositiveButton(R.string.yes, (dialog, which) -> onDeleteBook())
                        .setNeutralButton(R.string.no, (d,w)->{});
                builder.create().show();
                return true;
            case R.id.action_supply_book: {
                startBookSupplierDialogAsync();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onDeleteBook() {
        SupplierAccess sAccess = (SupplierAccess) access;
        new ProgressDialogAsyncTask<Void, Void, Void>(activity, R.string.updating_book_supplying) {
            @Override protected Void retrieveDataAsync(Void... params) {
                    sAccess.removeBookSupplier(oCurrentBookSupplier.get());
                    return null;
            }
            @Override protected void doByData(Void aVoid) {
                    Toast.makeText(activity,R.string.book_was_removed_from_supplier, Toast.LENGTH_SHORT).show();
                    suppliersListView.removeView(suppliersViewsMap.get(oCurrentBookSupplier.get()));
                    menu.findItem(R.id.action_remove_book).setVisible(false);
            }
        }.execute();
    }

    private void startBookSupplierDialogAsync() {
        new CancelableLoadingDialogAsyncTask<Void,Void,BookSupplier>(activity) {
            @Override
            protected BookSupplier retrieveDataAsync(Void... params) {
                return AccessManagerFactory.getInstance().getSupplierAccess()
                        .retrieveMyBookSupplier(book)
                        .orElseGet(() -> {
                            BookSupplier bs = new BookSupplier();
                            bs.setBookId(entityId);
                            return bs;
                        });
            }
            @Override
            protected void doByData(BookSupplier bookSupplier) {
                BookSupplierDialogFragment.newInstance(bookSupplier)
                        .show(getFragmentManager(), "BookSupplierDialogFragment");
            }

            @Override
            protected void onCancel() { /* Do nothing */ }
        }.execute();
    }

    @Override
    public void onBookReviewResult(boolean canceled, BookReview bookReview, Rating oldUserRating) {
        if (canceled) {
            userRatingBar.setRating(oldUserRating.getStars());
        } else {
            new ProgressDialogAsyncTask<Void, Void, Void>(activity) {
                @Override
                protected Void retrieveDataAsync(Void... params) {
                    // apply the customer details
                    CustomerAccess cAccess = (CustomerAccess) access;
                    cAccess.writeBookReview(bookReview);
                    return null;
                }
                @Override
                protected void doByData(Void aVoid) {
                    userBookReview = bookReview;
                    userRatingBar.setRating(userBookReview.getRating().getStars());
                    // message
                    Toast.makeText(activity, "The review was updated.", Toast.LENGTH_LONG).show();
                }
            }.execute();
        }

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
        boolean isNewBook = bookSupplier == null || bookSupplier.getId() == 0;
        switch (result) {
            case OK:
                new ProgressDialogAsyncTask<Void,Void,Void>(activity, R.string.updating_book_supplying) {
                    @Override
                    protected Void retrieveDataAsync(Void... params) {
                        if (isNewBook) {
                            sAccess.addBookSupplier(bookSupplier);
                        } else {
                            sAccess.updateBookSupplier(bookSupplier);
                        }
                        return null;
                    }

                    @Override
                    protected void doByData(Void aVoid) {
                        int messageRedId = isNewBook ? R.string.book_was_added_to_supplier : R.string.book_was_updated_to_supplier;
                        Toast.makeText(activity, messageRedId, Toast.LENGTH_SHORT).show();
                        startActivity(IntentsFactory.supplierBooksIntent(activity));
                    }
                }.execute(); break;
            case CANCEL: break;
        }
    }
}
