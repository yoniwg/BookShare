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
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.GeneralAccess;


/**
 * An abstract {@link Fragment} to use for {@link CustomerAccess} fragments subclass.
 *
 */
public abstract class AbstractFragment<T extends GeneralAccess> extends Fragment {

    private int fragmentId = getFragmentId();

    private int menuId = getMenuId();

    protected T access;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        access = (T) AccessManagerFactory.getInstance().getGeneralAccess();
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(fragmentId, container, false);
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
    abstract @LayoutRes int getFragmentId();

    /**
     * should return the menu resource ID.
     * if no menu is supported - return 0
     * @return
     */
    abstract @MenuRes int getMenuId();
}
