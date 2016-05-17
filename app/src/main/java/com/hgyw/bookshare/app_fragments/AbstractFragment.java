package com.hgyw.bookshare.app_fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;


/**
 *
 * @param <T> unchecked cast of the access.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractFragment<T extends GeneralAccess> extends Fragment {

    private @LayoutRes int fragmentLayoutId = getFragmentLayoutId();

    private @MenuRes int menuId = getMenuId();

    protected T access;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        access = (T) AccessManagerFactory.getInstance().getFullAccess();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(fragmentLayoutId, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(menuId == 0) return;
        inflater.inflate(menuId, menu);
    }

    /**
     * should return the fragment resource ID.
     * @return
     */
    abstract @LayoutRes int getFragmentLayoutId();

    /**
     * should return the menu resource ID.
     * if no menu is supported - return 0
     * @return
     */
    abstract @MenuRes int getMenuId();
}
