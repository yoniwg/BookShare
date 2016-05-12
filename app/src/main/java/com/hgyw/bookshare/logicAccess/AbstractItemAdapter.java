package com.hgyw.bookshare.logicAccess;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.hgyw.bookshare.BeautifulListItemView;
import com.hgyw.bookshare.Utility;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Supplier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haim7 on 12/05/2016.
 */
public abstract class AbstractItemAdapter<T,ItemView extends View> extends BaseAdapter {

    protected final Context context;
    protected final List<T> itemsList;
    private final ItemViewSupplier<ItemView> itemViewSupplier;

    protected AbstractItemAdapter(Context context, List<T> itemsList, ItemViewSupplier<ItemView> itemViewSupplier) {
        this.itemsList = itemsList;
        this.context = context;
        this.itemViewSupplier = itemViewSupplier;
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
    public ItemView getView(int position, View convertView, ViewGroup parent) {
        ItemView view;
        if (convertView == null) view = itemViewSupplier.get(context);
        else view = (ItemView) convertView;
        onNewItem(position, view);
        return view;
    }

    /**
     * Set view according to item-position
     * @param position
     * @param view
     */
    protected abstract void onNewItem(int position, ItemView view);

    public interface ItemViewSupplier<ItemView extends View> {
        ItemView get(Context context);
    }
}
