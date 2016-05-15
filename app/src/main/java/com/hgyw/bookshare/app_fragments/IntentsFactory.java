package com.hgyw.bookshare.app_fragments;

import android.content.Context;
import android.content.Intent;

import com.hgyw.bookshare.app_activities.EntityActivity;
import com.hgyw.bookshare.app_activities.MainActivity;
import com.hgyw.bookshare.app_activities.RegistrationActivity;
import com.hgyw.bookshare.app_activities.UserEditActivity;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

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
    public static final String ARG_REFRESH_LOGIN = "refreshLogin";
    public static final int GET_IMAGE_CODE = 0x44;


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

    public static Intent homeIntent(Context context, boolean refreshLogin) {
        Intent intent = newBookListIntent(context, null);
        if (refreshLogin) {
            intent.putExtra(ARG_REFRESH_LOGIN, true);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent homeIntent(Context context) {
        return homeIntent(context, false);
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

    public static Intent userDetailsIntent(Context context) {
        Intent intent = new Intent(context, UserEditActivity.class);
        User user = AccessManagerFactory.getInstance().getGeneralAccess().retrieveUserDetails();
        intent.putExtra(ARG_USER_DETAILS, user);
        return intent;
    }
}
