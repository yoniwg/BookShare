package com.hgyw.bookshare.app_fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.hgyw.bookshare.EnumAdapter;
import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.Utility;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;

import java.math.BigDecimal;

/**
 * Created by haim7 on 12/05/2016.
 */
public class BookQueryDialogFragment extends DialogFragment {

    private static final String ARG_DIALOG_BOOK_QUERY = "dialogBookQuery";

    public static BookQueryDialogFragment newInstance(BookQuery bookQuery) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIALOG_BOOK_QUERY, bookQuery);

        BookQueryDialogFragment fragment = new BookQueryDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BookQuery bookQuery = getArguments() == null ? null : (BookQuery) getArguments().getSerializable(ARG_DIALOG_BOOK_QUERY);

        // inflate and set view
        View view = onCreateView(getActivity().getLayoutInflater(), null, savedInstanceState);
        if (bookQuery != null) ObjectToViewAppliers.apply(view, bookQuery);

        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.dialog_title_book_query)
                .setPositiveButton(getString(R.string.filter), (dialog1, which) -> {
                    ObjectToViewAppliers.result(view, bookQuery);
                    Intent intent = IntentsFactory.newBookListIntent(getActivity(), bookQuery);
                    getActivity().startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> onCancel(dialog));

        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_book_query, container, false);
        Spinner genreSpinner = (Spinner) view.findViewById(R.id.genre_spinner);
        ArrayAdapter arrayAdapter = new EnumAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Book.Genre.values());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(arrayAdapter);
        return view;
    }
}
