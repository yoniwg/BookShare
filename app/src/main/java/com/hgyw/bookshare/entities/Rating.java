package com.hgyw.bookshare.entities;

/**
 * Created by Yoni on 3/25/2016.
 */
public enum Rating {
    EMPTY ,POOR, BAD, MEDIUM, GOOD, EXCELLENT;

    public int getStarts() {
        return ordinal();
    }
}
