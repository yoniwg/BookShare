package com.hgyw.bookshare;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

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
    private View view;
    private int result = CANCELED;

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
        BookReview bookReview = getArguments() == null ? null : (BookReview) getArguments().getSerializable(ARG_DIALOG_BOOK_REVIEW);
        if (bookReview == null) throw new IllegalArgumentException("The BookReviewDialogFragment should accept not-null bookReview object.");;

        ObjectToViewAppliers.apply(view, bookReview);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.rating_box_title)
                .setView(view)
                .setPositiveButton(R.string.rate, (dialog, which) -> {
                    // apply to view the bookReview
                    BookReview resultBookReview = new BookReview();
                    resultBookReview.setBookId(bookReview.getBookId());
                    resultBookReview.setCustomerId(bookReview.getCustomerId());
                    ObjectToViewAppliers.result(view, resultBookReview);
                    // apply the customer details
                    CustomerAccess access = AccessManagerFactory.getInstance().getCustomerAccess();
                    access.writeBookReview(resultBookReview);
                    result = resultBookReview.getRating().getStars();
                    // messege
                    Toast.makeText(getActivity(), "The review was updated.", Toast.LENGTH_LONG);
                })
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), result, null);
        }
    }

}
