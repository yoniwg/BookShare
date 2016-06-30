package com.hgyw.bookshare.app_drivers.extensions;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Simple implementation of TextWatcher in order to handle text changing
 * (in EditText fields, etc.)
 * Created by Yoni on 5/15/2016.
 */
public abstract class SimpleTextWatcher implements TextWatcher {



    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }


    public abstract void onTextChanged(CharSequence s, int start, int before, int count);


    @Override
    public void afterTextChanged(Editable s) {

    }
}
