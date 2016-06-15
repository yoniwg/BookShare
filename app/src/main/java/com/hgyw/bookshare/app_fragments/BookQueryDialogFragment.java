package com.hgyw.bookshare.app_fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.hgyw.bookshare.app_drivers.EnumAdapter;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.MultiSpinner;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Created by haim7 on 12/05/2016.
 */
public class BookQueryDialogFragment extends DialogFragment  {

    private static final String ARG_DIALOG_BOOK_QUERY = "dialogBookQuery";
    private static final String SAVE_KEY_SELECTED = "saveKeySelected";
    private MultiSpinner genreSpinner;

    /**
     *
     * @param bookQuery - will take new bookQuery if bookQuery==null.
     * @return
     */
    public static BookQueryDialogFragment newInstance(BookQuery bookQuery) {
        Bundle args = new Bundle();
        if (bookQuery == null) bookQuery = new BookQuery();
        args.putSerializable(ARG_DIALOG_BOOK_QUERY, bookQuery);

        BookQueryDialogFragment fragment = new BookQueryDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView1(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_book_query, container, false);
        genreSpinner = (MultiSpinner) view.findViewById(R.id.genre_spinner);
        Utility.setSpinnerToEnum(getActivity(), genreSpinner, Book.Genre.values());
        if (savedInstanceState != null) {
            boolean[] selected = savedInstanceState.getBooleanArray(SAVE_KEY_SELECTED);
            if (selected != null) genreSpinner.setSelected(selected);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBooleanArray(SAVE_KEY_SELECTED, genreSpinner.getSelected());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // inflate and set view
        View view = onCreateView1(getActivity().getLayoutInflater(), null, savedInstanceState);
        BookQuery bookQuery = getArguments() == null ? null : (BookQuery) getArguments().getSerializable(ARG_DIALOG_BOOK_QUERY);

        if (savedInstanceState == null) {
            if (bookQuery == null)
                throw new IllegalArgumentException("bookQuery should not be null, it should maintain by newInstance factory method.");
            ObjectToViewAppliers.apply(view, bookQuery);
        }

        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.dialog_title_book_query)
                .setPositiveButton(R.string.filter, (dialog1, which) -> {
                    ObjectToViewAppliers.result(view, bookQuery);
                    // set genres
                    boolean[] selected = genreSpinner.getSelected();
                    if (selected != null) {
                        Book.Genre[] genres = Book.Genre.values();
                        Set<Book.Genre> genresSet = bookQuery.getGenreSet();
                        genresSet.clear();
                        for (int i = 0; i < selected.length; i++) if (selected[i]) genresSet.add(genres[i]);
                    }

                    Intent intent = IntentsFactory.newBookListIntent(getActivity(), bookQuery);
                    getActivity().startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> onCancel(dialog));

        return builder.create();
    }

}
