package com.hgyw.bookshare;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_book_query, null);
        Spinner genreSpinner = (Spinner) view.findViewById(R.id.genre_spinner);
        defineSpinner(genreSpinner);
        if (bookQuery != null) updateObjectToView(bookQuery, view);


        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.dialog_title_book_query)
                .setNegativeButton(R.string.cancel, (dialog, which) -> onCancel(dialog))
                .setPositiveButton(getString(R.string.filter), (dialog1, which) -> {
                    BookQuery resultBookQuery = resultObjectFromView();
                    Intent intent = IntentsFactory.newBookListIntent(getActivity(), resultBookQuery);
                    getActivity().startActivity(intent);
                });

        return builder.create();
    }

    private static void updateObjectToView(BookQuery bookQuery, View view) {

        TextView titleView = (TextView) view.findViewById(R.id.title_query);
        TextView authorView = (TextView) view.findViewById(R.id.author_query);
        // TODO and spinner
        Spinner genreSpinner = (Spinner) view.findViewById(R.id.genre_spinner);
        TextView fromPriceView = (TextView) view.findViewById(R.id.from_price);
        TextView toPriceView = (TextView) view.findViewById(R.id.to_price);

        titleView.setText(bookQuery.getTitleQuery());
        authorView.setText(bookQuery.getAuthorQuery());
        fromPriceView.setText(Utility.moneyToNumberString(bookQuery.getBeginPrice()));
        toPriceView.setText(Utility.moneyToNumberString(bookQuery.getEndPrice()));
        Book.Genre genreSelection = bookQuery.getGenreSet().isEmpty() ? Book.Genre.GENERAL : bookQuery.getGenreSet().iterator().next();
        genreSpinner.setSelection(genreSelection.ordinal());
    }

    private BookQuery resultObjectFromView() {
        Dialog dialog = getDialog();
        BookQuery bookQuery = new BookQuery();

        TextView titleView = (TextView) dialog.findViewById(R.id.title_query);
        TextView authorView = (TextView) dialog.findViewById(R.id.author_query);
        // TODO and spinner
        Spinner genreSpinner = (Spinner) dialog.findViewById(R.id.genre_spinner);
        TextView fromPriceView = (TextView) dialog.findViewById(R.id.from_price);
        TextView toPriceView = (TextView) dialog.findViewById(R.id.to_price);

        bookQuery.setTitleQuery(titleView.getText().toString());
        bookQuery.setAuthorQuery(authorView.getText().toString());
        bookQuery.getGenreSet().clear();
        bookQuery.getGenreSet().add((Book.Genre) genreSpinner.getSelectedItem());
        try {
            bookQuery.setBeginPrice(new BigDecimal(fromPriceView.getText().toString()));
            bookQuery.setEndPrice(new BigDecimal(toPriceView.getText().toString()));
        } catch (NumberFormatException ignored) {}
        return bookQuery;
    }

    private void defineSpinner(Spinner genreSpinner) {
        ArrayAdapter arrayAdapter = new EnumAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Book.Genre.values());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(arrayAdapter);
    }

}
