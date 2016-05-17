package com.hgyw.bookshare.app_fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.StringRes;

import com.hgyw.bookshare.logicAccess.GeneralAccess;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class EntityFragment extends AbstractFragment<GeneralAccess> {

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
