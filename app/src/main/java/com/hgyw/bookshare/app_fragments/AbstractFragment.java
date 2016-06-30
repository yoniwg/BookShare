package com.hgyw.bookshare.app_fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.StringDef;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;


/**
 * Abstract fragment class performing basic method of fragment creating
 * @param <T> unchecked cast of the access.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractFragment<T extends GeneralAccess> extends Fragment implements TitleFragment {

    private final @LayoutRes int fragmentLayoutId;

    private final @MenuRes int menuId;

    private final @StringRes int titleId;

    protected T access;

    protected AbstractFragment(@LayoutRes int fragmentLayoutId, @MenuRes int menuId, @StringRes int titleId) {
        this.fragmentLayoutId = fragmentLayoutId;
        this.menuId = menuId;
        this.titleId = titleId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        access = (T) AccessManagerFactory.getInstance().getGeneralAccess();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(fragmentLayoutId, container, false);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(menuId != 0) inflater.inflate(menuId, menu);
    }

    @Override
    public @StringRes int getFragmentTitle() {
        return titleId;
    }
}
