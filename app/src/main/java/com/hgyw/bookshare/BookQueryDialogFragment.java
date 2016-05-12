package com.hgyw.bookshare;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;

import java.math.BigDecimal;

/**
 * Created by haim7 on 12/05/2016.
 */
public class BookQueryDialogFragment extends DialogFragment {

    private static final String ARG_DIALOG_BOOK_QUERY = "dialogBookQuery";

    BookQuery bookQuery;

    public static BookQueryDialogFragment newInstance(BookQuery bookQuery) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIALOG_BOOK_QUERY, bookQuery);

        BookQueryDialogFragment fragment = new BookQueryDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = new Bundle();
        if (savedInstanceState != null) bundle.putAll(savedInstanceState);
        if (getArguments() != null) bundle.putAll(getArguments());

        bookQuery = (BookQuery) bundle.getSerializable(ARG_DIALOG_BOOK_QUERY);
        if (bookQuery == null) throw new NullPointerException("the BookQuery should not be null.");

        // inflate and set view
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_book_query, null);
        Spinner genreSpinner = (Spinner) view.findViewById(R.id.genre_spinner);
        defineSpinner(genreSpinner);
        updateObjectToView(view);

        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.dialog_title_book_quary)
                .setNeutralButton(getString(R.string.filter), (dialog1, which) -> {
                    IntentsFactory.newBookListIntent(getActivity(), bookQuery);
                    Toast.makeText(BookQueryDialogFragment.this.getActivity(), bookQuery.toString(), Toast.LENGTH_LONG).show();
                });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateViewToObject();
        outState.putAll(getArguments());
    }


    private void updateObjectToView(View view) {
        TextView titleView = (TextView) view.findViewById(R.id.title_query);
        TextView authorView = (TextView) view.findViewById(R.id.author_query);
        // TODO and spinner
        Spinner genreSpinner = (Spinner) view.findViewById(R.id.genre_spinner);
        TextView fromPriceView = (TextView) view.findViewById(R.id.from_price);
        TextView toPriceView = (TextView) view.findViewById(R.id.to_price);

        titleView.setText(bookQuery.getTitleQuery());
        authorView.setText(bookQuery.getAuthorQuery());
        fromPriceView.setText(bookQuery.getBeginPrice().toString());
        toPriceView.setText(bookQuery.getEndPrice().toString());
        genreSpinner.setSelection(bookQuery.getGenreSet().ordinal());
    }

    private void updateViewToObject() {
        View view = getView();
        TextView titleView = (TextView) view.findViewById(R.id.title_query);
        TextView authorView = (TextView) view.findViewById(R.id.author_query);
        // TODO and spinner
        Spinner genreSpinner = (Spinner) view.findViewById(R.id.genre_spinner);
        TextView fromPriceView = (TextView) view.findViewById(R.id.from_price);
        TextView toPriceView = (TextView) view.findViewById(R.id.to_price);

        bookQuery.setTitleQuery(titleView.getText().toString());
        bookQuery.setAuthorQuery(authorView.getText().toString());
        bookQuery.setGenreQuery((Book.Genre) genreSpinner.getSelectedItem());
        try {
            bookQuery.setBeginPrice(new BigDecimal(fromPriceView.getText().toString()));
            bookQuery.setEndPrice(new BigDecimal(toPriceView.getText().toString()));
        } catch (NumberFormatException ignored) {}

    }

    private void defineSpinner(Spinner genreSpinner) {
        ArrayAdapter arrayAdapter = new EnumAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Book.Genre.values());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_multiple_choice);
        genreSpinner.setAdapter(arrayAdapter);
    }

    public BookQuery getBookQuery() {
        return bookQuery;
    }
}
