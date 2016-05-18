package com.hgyw.bookshare.app_fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_activities.EntityActivity;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.Rating;

/**
 * Created by haim7 on 13/05/2016.
 */

/**
 * Returns result by onActivityResult to target fragment with number of stars of rate
 * or DialogFragment.CANCELED if it was canceled.
 */
public class BookReviewDialogFragment extends DialogFragment {

    private final static String ARG_DIALOG_OLD_BOOK_REVIEW = "dialogOldBookReview";
    private final static String ARG_DIALOG_NEW_RATING = "dialogOldViewRating";
    private View view;
    private BookReview bookReview;
    private Rating oldRating;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_book_review, null);
        bookReview = getArguments() == null ? null : (BookReview) getArguments().getSerializable(ARG_DIALOG_OLD_BOOK_REVIEW);
        if (bookReview == null) throw new IllegalArgumentException("The BookReviewDialogFragment should accept not-null bookReview object.");;
        float newViewRating = getArguments().getFloat(ARG_DIALOG_NEW_RATING, 0);
        oldRating = bookReview.getRating();
        bookReview.setRating(Rating.of(newViewRating));

        ObjectToViewAppliers.apply(view, bookReview);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.rating_box_title)
                .setView(view)
                .setPositiveButton(R.string.rate, (dialog, which) -> sendResult(false))
                .create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        sendResult(true);
    }

    private void sendResult(boolean isCancel) {
        if (isCancel) {
            bookReview = null;
        } else {
            ObjectToViewAppliers.result(view, bookReview);
        }
        try {
            BookReviewResultListener resultListener =
                    (BookReviewResultListener) getFragmentManager().findFragmentByTag(EntityActivity.ENTITY_FRAGMENT_TAG);
            resultListener.onBookReviewResult(isCancel, bookReview, oldRating);
        } catch (NullPointerException | ClassCastException e) {
            throw new RuntimeException("Cannot find the target fragment and cast it to BookReviewResultListener.", e);
        }

    }

    public interface BookReviewResultListener {
        void onBookReviewResult(boolean canceled, BookReview bookReview, Rating oldUserRating);
    }


}
