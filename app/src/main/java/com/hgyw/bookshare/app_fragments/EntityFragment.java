package com.hgyw.bookshare.app_fragments;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.StringRes;

import com.hgyw.bookshare.logicAccess.GeneralAccess;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class EntityFragment extends AbstractFragment<GeneralAccess> {

    protected long entityId;
    private static final String ARG_ENTITY_ID = "entityId";

    public EntityFragment(@LayoutRes int fragmentLayoutId, @MenuRes int menuId, @StringRes int titleId) {
        super(fragmentLayoutId, menuId, titleId);
    }

    public static <T extends EntityFragment> T newInstance(Class<T> fragmentClass, long entityId) {
        Bundle args = new Bundle();
        args.putLong(ARG_ENTITY_ID, entityId);
        T fragment;
        try {
            fragment = fragmentClass.newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            String message = "Cannot instantiate EntityFragment of " + fragmentClass.getSimpleName();
            throw new IllegalArgumentException(message, e);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entityId = getArguments() == null ? 0 : getArguments().getLong(ARG_ENTITY_ID);
        if (entityId == 0) {
            throw new IllegalArgumentException("Entity fragment was instantiated with " + ARG_ENTITY_ID + "=0 (or null).");
        }
    }
}
