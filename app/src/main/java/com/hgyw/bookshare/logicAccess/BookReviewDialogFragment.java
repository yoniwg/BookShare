package com.hgyw.bookshare.logicAccess;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.BookReview;

/**
 * Created by haim7 on 13/05/2016.
 */
public class BookReviewDialogFragment extends DialogFragment {

    private final static String ARG_DIALOG_BOOK_REVIEW = "dialogBookReview";
    public static final int CANCELED = -1;
    private View view;

    public static BookReviewDialogFragment newInstance(BookReview bookReview) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIALOG_BOOK_REVIEW, bookReview);

        BookReviewDialogFragment fragment = new BookReviewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.book_review_component, null);
        BookReview bookReview = getArguments() == null ? null : (BookReview) getArguments().getSerializable(ARG_DIALOG_BOOK_REVIEW);
        if (bookReview != null) ObjectToViewAppliers.apply(view, bookReview);
        else throw new IllegalArgumentException("The BookReviewDialogFragment should accept not-null bookReview object.");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.ratingBoxTitle )
                .setView(view)
                .setPositiveButton(R.string.rate, (dialog, which) -> {
                    CustomerAccess access = AccessManagerFactory.getInstance().getCustomerAccess();
                    BookReview resultBookReview = new BookReview();
                    resultBookReview.setBookId(bookReview.getBookId());
                    ObjectToViewAppliers.result(view, resultBookReview);
                    access.writeBookReview(resultBookReview);
                    Toast.makeText(getActivity(), "The review was updated.", Toast.LENGTH_LONG);
                })
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), 0/*TODO*/, null);
        }
        Toast.makeText(getActivity(), "dismiss", Toast.LENGTH_SHORT).show();
    }

}
