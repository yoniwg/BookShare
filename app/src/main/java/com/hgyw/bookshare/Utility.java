package com.hgyw.bookshare;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by haim7 on 11/05/2016.
 */
public class Utility {

    /**
     *
     * @param idsClass
     * @param idString
     * @return 0 if not fount.
     */
    public static int findIdByString(Class idsClass, String idString) {
        try {
            return (int) idsClass.getField(idString).get(null);
        } catch (IllegalAccessException | NoSuchFieldException | ClassCastException e) {
            return 0;
        }
    }

    public static String findStringResourceOfEnum(Context context, Enum<?> enumValue) {
        String idName = enumValue.getClass().getSimpleName().toLowerCase() + "_" + enumValue.name().toLowerCase();
        int id = findIdByString(R.string.class, idName);
        String string = (id == 0 ? null : context.getString(id));
        if (string == null) {
            string = enumValue.name();//.toLowerCase().replace('_', ' ');
        }
        return string;
    }

    public static boolean setImageById(ImageView imageView, long imageId) {
        if (imageId == 0) return false;
        ImageEntity imageEntity = AccessManagerFactory.getInstance().getGeneralAccess().retrieve(ImageEntity.class, imageId);
        byte[] bytes = imageEntity.getBytes();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageView.setImageBitmap(bitmap);
        return true;
    }

    public static String moneyToString(BigDecimal minPrice) {
        final char newShekelSign = '\u20AA';
        return minPrice.setScale(2).toString() + newShekelSign;
    }

    public static String moneyRangeToString(BigDecimal minPrice, BigDecimal maxPrice) {
        String minString = moneyToString(minPrice);
        String maxString = moneyToString(maxPrice);
        if (minString.equals(maxString)) return minString;
        return minString + " \u2014 " + maxString;
    }

    public static String usernameToString(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
