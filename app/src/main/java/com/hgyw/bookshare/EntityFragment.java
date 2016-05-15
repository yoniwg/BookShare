package com.hgyw.bookshare;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.StringRes;
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
public abstract class EntityFragment extends Fragment {

    protected long entityId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entityId = getArguments() == null ? 0 : getArguments().getLong(IntentsFactory.ARG_ENTITY_ID);
        if (entityId == 0) {
            throw new IllegalArgumentException("Entity fragment was instantiated with ARG_ENTITY_ID=0 (or null).");
        }
    }

    public abstract @StringRes int getTitleResource();
}
