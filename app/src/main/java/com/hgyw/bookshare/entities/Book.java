package com.hgyw.bookshare.entities;

import java.util.Objects;

/**
 * Created by Yoni on 3/15/2016.
 */
public final class Book extends Entity{

    public enum Genre {GENERAL, ACTION, ROMANCE, SCIENCE, SCIENCE_FICTION,
        DRAMA, SATIRE, CHILDREN, COMICS, BIOGRAPHIES, FANTASY, HEALTH}

    private String title;
    private String bookAbstract;
    private String author;
    private Genre genre = Genre.GENERAL;
    private long imageId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBookAbstract() {
        return bookAbstract;
    }

    public void setBookAbstract(String bookAbstract) {
        this.bookAbstract = bookAbstract;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = Objects.requireNonNull(genre);
    }

    @Override
    public String shortDescription() {
        return super.shortDescription() + " '" + getTitle() + "'";
    }

    public long getImageId() {
        return imageId;
    }

    public void setImageId(long imageId) {
        this.imageId = imageId;
    }
}
