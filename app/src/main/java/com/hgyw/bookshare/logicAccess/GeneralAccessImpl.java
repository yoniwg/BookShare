package com.hgyw.bookshare.logicAccess;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import com.hgyw.bookshare.dataAccess.DataAccess;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;

/**
 * Created by Yoni on 3/18/2016.
 */
class GeneralAccessImpl implements GeneralAccess {

    final protected DataAccess dataAccess;
    final private User currentUser;

    protected void requireItsMeForAccess(UserType userType, long userId) {
        if (currentUser.getUserType() != userType || currentUser.getId() != userId) {
            throw new IllegalArgumentException("The current user has not access to manipulate other users.");
        }
    }

    public GeneralAccessImpl(DataAccess dataAccess, User currentUser) {
        this.dataAccess = dataAccess;
        this.currentUser = currentUser;
    }

    @Override
    public List<Book> findBooks(BookQuery query) {
        return dataAccess.findBooks(query);
    }

    @Override
    public BookSummary getBookSummary(Book book) {
        return dataAccess.getBookSummary(book);
    }

    @Override
    public List<Book> findSpecialOffers(int limit) {
        return dataAccess.findSpecialOffers(currentUser, limit);
    }

    @Override
    public List<BookReview> findBookReviews(Book book) {
        return dataAccess.findEntityReferTo(BookReview.class, book);
    }

    @Override
    public List<BookSupplier> findBookSuppliers(Book book) {
        return dataAccess.findEntityReferTo(BookSupplier.class, book);
    }

    @Override
    public List<BookSupplier> findBooksOfSuppliers(Supplier supplier) {
        return dataAccess.findEntityReferTo(BookSupplier.class, supplier);
    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityClass, long entityId) {
        return dataAccess.retrieve(entityClass, entityId);
    }

    public <T extends User> T retrieveUserDetails(T currentUser) {
        return (T) dataAccess.retrieve(currentUser);
    }

    public <T extends User> void updateUserDetails(T currentUser, T newDetails) {
        requireItsMeForAccess(newDetails.getUserType(), newDetails.getId());
        newDetails.setCredentials(((User) dataAccess.retrieve(currentUser)).getCredentials()); // Avoid change credentials by this method.
        dataAccess.update(newDetails);
    }

}
