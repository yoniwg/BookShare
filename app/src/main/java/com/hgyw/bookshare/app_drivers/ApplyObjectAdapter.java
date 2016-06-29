package com.hgyw.bookshare.app_drivers;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.hgyw.bookshare.entities.Entity;

import java.util.Collection;
import java.util.List;

/**
 * Abstract adapter that gets list and layout-id, and when item appears, it inflates layout to view,
 * and call to method {@link ApplyObjectAdapter#applyOnView} for set the view by the item. <br>
 * it extends {@link ArrayAdapter} as you see.
 *
 */
public abstract class ApplyObjectAdapter<T> extends ArrayAdapter<T> {

    protected final LayoutInflater inflater;
    protected final List<T> itemsList;
    protected final int itemLayoutId;

    protected ApplyObjectAdapter(Context context, @LayoutRes int itemLayoutId, List<T> itemsList) {
        super(context, itemLayoutId, itemsList);
        this.itemsList = itemsList;
        this.inflater = LayoutInflater.from(context);
        this.itemLayoutId = itemLayoutId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(itemLayoutId, parent, false);
        }
        applyOnView(view, position);
        return view;
    }

    /**
     * sets view by position of item. <br>
     * you should get the item by method {@link ApplyObjectAdapter#getItem(int)}.
     */
    protected abstract void applyOnView(View view, int position);

}
