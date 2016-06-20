package com.hgyw.bookshare.entities;

/**
 * Created by Yoni on 3/25/2016.
 */
public enum Rating {
    EMPTY ,POOR, BAD, MEDIUM, GOOD, EXCELLENT;

    public int getStars() {
        return ordinal();
    }

    /**
     * @throws IllegalArgumentException if start is not legal
     */
    public static Rating of(int stars) {
        if (stars <= 0 || stars > values().length) throw new IllegalArgumentException("The stars parameter is not legal. stars=" + stars);
        return values()[stars];
    }
    public static Rating of(float rating) {
        return of((int) rating);
    }
}
