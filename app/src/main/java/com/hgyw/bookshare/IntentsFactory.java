package com.hgyw.bookshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.Cart;

/**
 * Created by haim7 on 12/05/2016.
 */
public class IntentsFactory {

    public static final String ARG_FRAGMENT_CLASS = "fragmentClass";
    public static final String ARG_BOOK_QUERY = "bookQuery";
    // public static final String ARG_CART = "argCart";
    public static final String ARG_ENTITY_ID = "id";
    public static final String ARG_ENTITY_TYPE = "entityType";
    public static final String ARG_USER_DETAILS = "userDetails";


    public static Intent newBookListIntent(Context context, BookQuery bookQuery) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_FRAGMENT_CLASS, BooksListFragment.class);
        intent.putExtra(ARG_BOOK_QUERY, bookQuery);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }


    public static Intent newCartIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_FRAGMENT_CLASS, CartFragment.class);
        intent.putExtra(CartFragment.IS_AMOUNT_CAN_MODIFY, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }


    public static Intent newEntityIntent(Context context, IdReference idReference) {
        Intent intent = new Intent(context, EntityActivity.class);
        intent.putExtra(ARG_ENTITY_TYPE, idReference.getEntityType());
        intent.putExtra(ARG_ENTITY_ID, idReference.getId());
        return intent;
    }

    public static Intent homeIntent(Context context) {
        return newBookListIntent(context, null);
    }

    public static Intent newTransactionIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_FRAGMENT_CLASS, TransactionFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static Intent newRegistrationIntent(Context context, User user) {
        Intent intent = new Intent(context, RegistrationActivity.class);
        intent.putExtra(ARG_USER_DETAILS, user);
        return intent;
    }
}
