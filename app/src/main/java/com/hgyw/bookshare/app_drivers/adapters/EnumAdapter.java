package com.hgyw.bookshare.app_drivers.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hgyw.bookshare.app_drivers.utilities.Utility;

/**
 * Adapter that gets enum values, and show them in list as ArrayAdapter.
 * The string of enums value get by {@link Utility#findStringResourceOfEnum}() method.
 */
public class EnumAdapter<E extends Enum<E>> extends ArrayAdapter<E> {
    String[] strings;
    {
        strings = new String[getCount()];
        for (int i = 0; i < strings.length; i++) {
            E enumValue = getItem(i);
            strings[i] = Utility.findStringResourceOfEnum(getContext(), enumValue);
        }
    }

    /**
     * @param context the context
     * @param resource layout resource
     * @param objects enum values
     */
    public EnumAdapter(Context context, @LayoutRes int resource, E[] objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getView(position, convertView, parent);
        textView.setText(strings[position]);
        return textView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
        textView.setText(strings[position]);
        return textView;
    }
}
