package com.hgyw.bookshare.app_fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.BookReview;

/**
 * Created by haim7 on 13/05/2016.
 */

/**
 * Returns result by onActivityResult to target fragment with number of stars of rate
 * or DialogFragment.CANCELED if it was canceled.
 */
public class BookReviewDialogFragment extends DialogFragment {

    private final static String ARG_DIALOG_BOOK_REVIEW = "dialogBookReview";
    public static final int CANCELED = -1;
    public static final String ARG_RESULT_OBJECT = "resultObject";
    private View view;
    private BookReview bookReview;

    public static BookReviewDialogFragment newInstance(BookReview bookReview) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIALOG_BOOK_REVIEW, bookReview);

        BookReviewDialogFragment fragment = new BookReviewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_book_review, null);
        bookReview = getArguments() == null ? null : (BookReview) getArguments().getSerializable(ARG_DIALOG_BOOK_REVIEW);
        if (bookReview == null) throw new IllegalArgumentException("The BookReviewDialogFragment should accept not-null bookReview object.");;

        ObjectToViewAppliers.apply(view, bookReview);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.rating_box_title)
                .setView(view)
                .setPositiveButton(R.string.rate, (dialog, which) -> {
                    ObjectToViewAppliers.result(view, bookReview);
                    Intent resultIntent = new Intent(); resultIntent.putExtra(ARG_RESULT_OBJECT, bookReview);
                    sendResult(0);
                })
                .create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        sendResult(CANCELED);
    }

    private void sendResult(int resultCode) {
        Intent resultIntent = new Intent(); resultIntent.putExtra(ARG_RESULT_OBJECT, bookReview);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, resultIntent);
    }
}
