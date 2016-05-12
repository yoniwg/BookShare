package com.hgyw.bookshare;

import android.app.Activity;
import android.content.Intent;

import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.logicAccess.Cart;

/**
 * Created by haim7 on 12/05/2016.
 */
public class IntentsFactory {

    public static final String ARG_FRAGMENT_CLASS = "fragmentClass";
    public static final String ARG_BOOK_QUERY = "bookQuery";
    public static final String ARG_CART = "argCart";


    public static Intent newBookListIntent(Activity activity, BookQuery bookQuery) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra(ARG_FRAGMENT_CLASS, BooksListFragment.class);
        intent.putExtra(ARG_BOOK_QUERY, bookQuery);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }


    public static Intent newCartIntent(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra(ARG_FRAGMENT_CLASS, CartFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }
}
