package com.hgyw.bookshare.app_fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ListenerSupplierHelper;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.entities.BookSupplier;

/**
 * Created by haim7 on 23/05/2016.
 */
public class BookSupplierDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String ARG_BOOK_SUPPLIER = "bookSupplier";
    private ResultListener listener;
    private View view;
    private BookSupplier bookSupplier;

    public static BookSupplierDialogFragment newInstance(BookSupplier bookSupplier) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOOK_SUPPLIER, bookSupplier);
        BookSupplierDialogFragment fragment = new BookSupplierDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = ListenerSupplierHelper.getListenerFromActivity(ResultListener.class, getActivity());
        bookSupplier = getArguments() == null ? null : (BookSupplier) getArguments().getSerializable(ARG_BOOK_SUPPLIER);
        if (bookSupplier == null) throw new IllegalArgumentException("The BookSupplierDialogFragment should accept non-null BookSupplier object.");
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_booksupplier, null);
        ObjectToViewAppliers.apply(view, bookSupplier);
        return new AlertDialog.Builder(getActivity()).setView(view)
                .setTitle("Update Yours Book")
                .setPositiveButton(R.string.ok, this)
                .setNeutralButton(R.string.delete, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
    }

    private void onOkPressed() {
        ObjectToViewAppliers.result(view, bookSupplier);
        listener.onBookSupplierResult(ResultListener.ResultCode.OK, bookSupplier);
    }

    private void onDeletePressed() {
        listener.onBookSupplierResult(ResultListener.ResultCode.DELETE, bookSupplier);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        listener.onBookSupplierResult(ResultListener.ResultCode.CANCEL, null);
        super.onCancel(dialog);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE: onOkPressed(); break;
            case Dialog.BUTTON_NEUTRAL: onDeletePressed(); break;
            case Dialog.BUTTON_NEGATIVE: dismiss(); break;
        }
    }

    public interface ResultListener {
        enum ResultCode {
            OK, CANCEL, DELETE
        }

        void onBookSupplierResult(ResultCode result, BookSupplier bookSupplier);
    }

}
