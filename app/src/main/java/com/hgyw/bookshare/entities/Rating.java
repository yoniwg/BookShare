package com.hgyw.bookshare.entities;

/**
 * Created by Yoni on 3/25/2016.
 */
public enum Rating {
    EMPTY ,POOR, BAD, MEDIUM, GOOD, EXCELLENT;

    public int getStars() {
        return ordinal();
    }

    public static Rating of(int stars) {
        return values()[stars];
    }
    public static Rating of(float rating) {
        return of((int) rating);
    }
}
