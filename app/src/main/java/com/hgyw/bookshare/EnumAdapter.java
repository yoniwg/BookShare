package com.hgyw.bookshare;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by haim7 on 11/05/2016.
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

    public EnumAdapter(Context context, int resource, E[] objects) {
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
