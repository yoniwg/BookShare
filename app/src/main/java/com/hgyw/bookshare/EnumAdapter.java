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
            //strings[i] = generateString(getItem(i));
        }
    }
    public EnumAdapter(Context context, int resource, int textViewResourceId, List<E> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public EnumAdapter(Context context, int resource, List<E> objects) {
        super(context, resource, objects);
    }

    public EnumAdapter(Context context, int resource, int textViewResourceId, E[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public EnumAdapter(Context context, int resource, E[] objects) {
        super(context, resource, objects);
    }

    public EnumAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public EnumAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return manipulateText(position, super.getView(position, convertView, parent));
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return manipulateText(position, super.getDropDownView(position, convertView, parent));
    }

    // update text of enum value to resources string
    private View manipulateText(int itemPosition, View textViewToSet) {
        ((TextView) textViewToSet).setText(generateString(getItem(itemPosition)));
        return textViewToSet;
    }

    protected String generateString(Enum<E> enumValue) {
        return Utility.findStringResourceOfEnum(getContext(), enumValue);
    }
}
