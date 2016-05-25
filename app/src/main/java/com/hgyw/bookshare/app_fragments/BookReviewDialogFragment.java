package com.hgyw.bookshare.app_fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.TextView;

import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ListenerSupplierHelper;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.Rating;

/**
 * Created by haim7 on 13/05/2016.
 */

/**
 * Throws ClassCastException if the activity cannot supply BookResultListener (by itself or by ListenerSupplier).
 */
public class BookReviewDialogFragment extends DialogFragment {

    private final static String ARG_DIALOG_OLD_BOOK_REVIEW = "dialogOldBookReview";
    private final static String ARG_DIALOG_NEW_RATING = "dialogOldViewRating";
    private Rating oldRating;
    private BookReviewResultListener resultListener;
    private BookReview bookReview;

    /**
     * Factory method for this fragment class.
     * @param oldBookReview - will not change by this fragment.
     * @param newViewRating the float value of new rating
     * @return the new fragment object.
     */
    public static BookReviewDialogFragment newInstance(BookReview oldBookReview, float newViewRating) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIALOG_OLD_BOOK_REVIEW, oldBookReview.clone());
        args.putFloat(ARG_DIALOG_NEW_RATING, newViewRating);
        BookReviewDialogFragment fragment = new BookReviewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resultListener = ListenerSupplierHelper.getListenerFromActivity(BookReviewResultListener.class, getActivity());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // inflate view
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_book_review, null, false);

        // retrieve fragment's arguments
        bookReview = getArguments() == null ? null : (BookReview) getArguments().getSerializable(ARG_DIALOG_OLD_BOOK_REVIEW);
        if (bookReview == null) throw new IllegalArgumentException("The BookReviewDialogFragment should accept not-null bookReview object.");;
        // we save the book-review instance to update it at the end, and save the values that the view doesn't change, like id.
        float newViewRating = getArguments().getFloat(ARG_DIALOG_NEW_RATING, 0);
        oldRating = bookReview.getRating();
        bookReview.setRating(Rating.of(newViewRating));

        // update view data
        ObjectToViewAppliers.apply(view, bookReview);

        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.rating_box_title)
                .setView(view)
                .setPositiveButton(R.string.rate, (dialog, which) -> {
                    ObjectToViewAppliers.result(view, bookReview);
                    resultListener.onBookReviewResult(false, bookReview, oldRating);
                })
                .create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        resultListener.onBookReviewResult(true, null, oldRating);
    }

    public interface BookReviewResultListener {
        void onBookReviewResult(boolean canceled, BookReview bookReview, Rating oldUserRating);
    }


}
