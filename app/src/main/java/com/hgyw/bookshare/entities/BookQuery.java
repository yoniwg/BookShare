package hgyw.com.bookshare.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Yoni on 3/15/2016.
 */
public class BookQuery{

    public enum SortByProperty{TITLE, AUTHOR, PRICE, POPULARITY}

    private String titleQuery = "";

    private String authorQuery = "";

    private Book.Genre genreQuery = null;

    private BigDecimal beginPrice = null;
    private BigDecimal endPrice = null;

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

    public void setEndPrice(BigDecimal endPrice) {
        this.endPrice = endPrice;
    }

    public List<SortByProperty> getSortByPropertyList() {
        return sortByPropertyList;
    }

    public void addPrioritySortStack(SortByProperty sortByProperty) {
        sortByPropertyList.add(sortByProperty);
    }
}
