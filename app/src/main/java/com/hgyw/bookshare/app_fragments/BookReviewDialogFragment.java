package com.hgyw.bookshare.app_fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;

import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.Rating;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

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

    /*public static BookReviewDialogFragment newInstance(BookReview bookReview, float oldViewRating) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIALOG_BOOK_REVIEW, bookReview);
        args.putFloat(ARG_DIALOG_OLD_VIEW_RATING, oldViewRating);
        BookReviewDialogFragment fragment = new BookReviewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }*/
    public static BookReviewDialogFragment newInstance(BookReview bookReview, float newViewRating) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIALOG_OLD_BOOK_REVIEW, bookReview);
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
        bookReview.setRating(Rating.ofStars((int) newViewRating));

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
        BookReviewResultListener resultListener = (BookReviewResultListener) getActivity().getFragmentManager().findFragmentById(R.id.fragment_container);
        resultListener.onBookReviewResult(isCancel, bookReview, oldRating);
    }

    public interface BookReviewResultListener {
        void onBookReviewResult(boolean canceled, BookReview bookReview, Rating oldUserRating);
    }


}
