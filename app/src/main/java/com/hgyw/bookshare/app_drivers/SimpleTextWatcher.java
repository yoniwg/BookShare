package com.hgyw.bookshare.app_drivers;

import android.text.Editable;
import android.text.TextWatcher;

/**
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
