package com.hgyw.bookshare;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class EntityFragment extends Fragment {

    private static final Map<Class<? extends Entity>, Class<? extends EntityFragment>> entityFragmentMap = new HashMap<>();
    static {
        entityFragmentMap.put(Book.class, BookFragment.class);
    }

    protected long entityId;

    protected EntityFragment() {}

    public static EntityFragment newInstance(Class entityType, long id) {
        Class<? extends EntityFragment> fragmentClass = entityFragmentMap.get(entityType);
        if (fragmentClass == null) {
            throw new IllegalArgumentException("No EntityFragment for " + entityType);
        }
        try {
            EntityFragment fragment = fragmentClass.newInstance();
            Bundle args = new Bundle();
            args.putLong(EntityActivity.ARG_ENTITY_ID, id);
            fragment.setArguments(args);
            return fragment;
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            String message = "Cannot instantiate EntityFragment of " + fragmentClass.getSimpleName();
            throw new IllegalArgumentException(message, e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entityId = getArguments().getLong(EntityActivity.ARG_ENTITY_ID);
    }
}
