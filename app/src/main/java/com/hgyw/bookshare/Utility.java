package com.hgyw.bookshare;

import android.content.Context;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Book;

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

}
