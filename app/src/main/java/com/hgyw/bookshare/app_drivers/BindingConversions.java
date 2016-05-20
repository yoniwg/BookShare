package com.hgyw.bookshare.app_drivers;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.databinding.InverseBindingAdapter;
import android.graphics.drawable.ColorDrawable;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;

/**
 * Created by haim7 on 11/05/2016.
 */
public class BindingConversions {

    @BindingConversion
    public static ColorDrawable convertColorToDrawable(int color) {
        return new ColorDrawable(color);
    }


    public static BigDecimal convertStringToBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static BigDecimal getBigDecimalAsString(TextView view) {
        return convertStringToBigDecimal(view.getText().toString());
    }


    @BindingConversion
    public static String convertBigDecimalToString(BigDecimal value) {
        return value.toString();
    }

}
