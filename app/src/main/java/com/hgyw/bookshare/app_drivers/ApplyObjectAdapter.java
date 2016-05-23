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
 * Created by haim7 on 12/05/2016.
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
    public long getItemId(int position) {
        T item = getItem(position);
        if (item instanceof Entity) {
            long itemId = ((Entity) item).getId();
            return itemId > 0 ? itemId : -position;
        }
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if ((view = convertView) == null) {
            view = inflater.inflate(itemLayoutId, parent, false);
        }
        applyOnView(view, position);
        return view;
    }

    protected abstract void applyOnView(View view, int position);

}
