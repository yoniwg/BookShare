package com.hgyw.bookshare;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.Collection;
import java.util.List;

/**
 * Created by haim7 on 12/05/2016.
 */
public abstract class ApplyObjectAdapter<T> extends BaseAdapter {

    protected final LayoutInflater inflater;
    protected final List<T> itemsList;
    protected final int itemLayoutId;

    protected ApplyObjectAdapter(Context context, @LayoutRes int itemLayoutId, List<T> itemsList) {
        this.itemsList = itemsList;
        this.inflater = LayoutInflater.from(context);
        this.itemLayoutId = itemLayoutId;
    }

    @Override
    public int getCount() {
        return itemsList.size();
    }

    @Override
    public T getItem(int position) {
        return itemsList.get(position);
    }

    @Override
    public long getItemId(int position) {
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
