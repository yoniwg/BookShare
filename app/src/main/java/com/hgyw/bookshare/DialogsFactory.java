package com.hgyw.bookshare;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hgyw.bookshare.databinding.DialogBookQueryBinding;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;

/**
 * Created by haim7 on 11/05/2016.
 */
public class DialogsFactory {

    private final Activity activity;

    private DialogsFactory(Activity activity) {
        this.activity = activity;
    }

    public static DialogsFactory by(Activity activity) {
        return new DialogsFactory(activity);
    }

    public Dialog newBookQueryDialog(BookQuery bookQuery) {
        DialogBookQueryBinding binding = DialogBookQueryBinding.inflate(activity.getLayoutInflater());
        binding.setQuery(bookQuery);

        // spinner is not use binding, so we have to set it manually
        Spinner genreSpinner = (Spinner) binding.getRoot().findViewById(R.id.genre_spinner);
        ArrayAdapter<Book.Genre> arrayAdapter = new EnumAdapter<>(activity, android.R.layout.simple_spinner_item, Book.Genre.values());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.sort(Book.Genre::compareTo);
        genreSpinner.setAdapter(arrayAdapter);

        genreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Book.Genre genre = (Book.Genre) parent.getItemAtPosition(position);
                bookQuery.setGenreQuery(genre);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        return builder.setView(binding.getRoot())
                .setTitle(R.string.dialog_title_book_quary)
                .setNeutralButton(activity.getString(R.string.filter), (dialog1, which) -> {
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.putExtra(MainActivity.ARG_FRAGMENT_CLASS, BooksListFragment.class);
                    intent.putExtra(BooksListFragment.ARG_BOOK_QUERY, bookQuery);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                    Toast.makeText(activity, bookQuery.toString(), Toast.LENGTH_LONG).show();
                })
                .create();
    }

}
