package com.hgyw.bookshare.app_drivers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.annimon.stream.Stream;
import com.hgyw.bookshare.app_activities.*;
import com.hgyw.bookshare.app_fragments.*;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;

/**
 * Created by haim7 on 12/05/2016.
 */
public class IntentsFactory {

    public static final String ARG_FRAGMENT_CLASS = "fragmentClass";
    public static final String ARG_BOOK_QUERY = "bookQuery";
    // public static final String ARG_CART = "argCart";
    public static final String ARG_USER_DETAILS = "userDetails";
    public static final String ARG_REFRESH_LOGIN = "refreshLogin";
    public static final int GET_IMAGE_CODE = 0x44;

    private static final Map<Class<? extends Entity>, Class<? extends EntityFragment>> entityFragmentMap = new HashMap<>();
    static {
        entityFragmentMap.put(Book.class, BookFragment.class);
        entityFragmentMap.put(User.class, SupplierFragment.class);
        //TODO entityFragmentMap.put(Order.class, OrderFragment.class);
    }

    public static Class<? extends EntityFragment> getEntityFragment(Class<? extends Entity> entityType) {
        Class<? extends EntityFragment> fragmentClass = entityFragmentMap.get(entityType);
        if (fragmentClass == null) {
            throw new IllegalArgumentException("No EntityFragment for " + entityType);
        }
        return fragmentClass;
    }

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
        intent.putExtra(CartFragment.IS_MAIN_FRAGMENT, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }


    public static Intent newEntityIntent(Context context, IdReference idReference) {
        Intent intent = new Intent(context, EntityActivity.class);
        intent.setData(uriOf(idReference));
        return intent;
    }

    public static Uri uriOf(IdReference idReference) {
        Class entityClass = idReference.getEntityType();
        long entityId = idReference.getId();
        return new Uri.Builder().path(entityClass.getSimpleName() + "/" + entityId).build();
    }

    public static IdReference idReferenceFrom(Uri uri) throws IllegalFormatException {
        String[] path = uri.getPath().split("/");
        if (path.length < 2) {
            throw new IllegalArgumentException("The uri should include legal id value");
        }
        String entityName = path[0];
        long entityId;
        try {
            entityId = Long.valueOf(path[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The uri should include legal id value.", e);
        }
        Class<? extends Entity> entityType = Stream.of(entityFragmentMap.keySet())
                .filter(type -> type.getSimpleName().equalsIgnoreCase(entityName))
                .findFirst().orElse(null);
        return IdReference.of(entityType, entityId);
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
        Intent intent = new Intent(context, TransactionActivity.class);
        return intent;
    }

    public static Intent newRegistrationIntent(Context context) {
        Intent intent = new Intent(context, UserRegistrationActivity.class);
        return intent;
    }

    public static Intent userDetailsIntent(Context context) {
        return new Intent(context, UserEditActivity.class);
    }

    public static Intent supplierOrdersIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_FRAGMENT_CLASS, SupplierOrdersFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static Intent newOldOrderIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_FRAGMENT_CLASS, OldOrdersFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static Intent editBookIntent(Context context, long id) {
        Intent intent = new Intent(context, BookEditActivity.class);
        intent.setData(uriOf(IdReference.of(Book.class, id)));
        return intent;
    }
}
