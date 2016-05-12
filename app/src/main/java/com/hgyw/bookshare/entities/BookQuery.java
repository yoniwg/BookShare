package com.hgyw.bookshare.entities;

import android.databinding.BindingAdapter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Yoni on 3/15/2016.
 */
public class BookQuery implements Serializable {

    public enum SortByProperty{TITLE, AUTHOR, PRICE, POPULARITY}

    private String titleQuery = "";

    private String authorQuery = "";

    private Book.Genre genreQuery = null;

    private BigDecimal beginPrice = BigDecimal.ZERO;
    private BigDecimal endPrice = new BigDecimal(1000);

    private List<SortByProperty> sortByPropertyList = new LinkedList<>();

    public String getTitleQuery() {
        return titleQuery;
    }

    public void setTitleQuery(String titleQuery) {
        this.titleQuery = titleQuery;
    }

    public String getAuthorQuery() {
        return authorQuery;
    }

    public void setAuthorQuery(String authorQuery) {
        this.authorQuery = authorQuery;
    }

    public Book.Genre getGenreQuery() {
        return genreQuery;
    }

    public void setGenreQuery(Book.Genre genreQuery) {
        this.genreQuery = genreQuery;
    }

    public BigDecimal getBeginPrice() {
        return beginPrice;
    }

    public BigDecimal getEndPrice() {
        return endPrice;
    }

    public void setBeginPrice(BigDecimal beginPrice) {
        this.beginPrice = beginPrice;
    }

    public void setBeginPrice(String beginPrice) {
        this.beginPrice = new BigDecimal(beginPrice);
    }

    public void setEndPrice(BigDecimal endPrice) {
        this.endPrice = endPrice;
    }

    public void setEndPrice(String endPrice) {
        setEndPrice(new BigDecimal(endPrice));
    }

    public List<SortByProperty> getSortByPropertyList() {
        return sortByPropertyList;
    }

    public void addPrioritySortStack(SortByProperty sortByProperty) {
        sortByPropertyList.add(sortByProperty);
    }

    @Override
    public String toString() {
        return "BookQuery{" +
                "endPrice=" + endPrice +
                ", beginPrice=" + beginPrice +
                ", genreQuery=" + genreQuery +
                ", authorQuery='" + authorQuery + '\'' +
                ", titleQuery='" + titleQuery + '\'' +
                '}';
    }
}
