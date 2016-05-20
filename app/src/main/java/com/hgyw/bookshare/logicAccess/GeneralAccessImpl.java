package com.hgyw.bookshare.logicAccess;

import java.text.MessageFormat;
import java.util.List;

import com.hgyw.bookshare.dataAccess.DataAccess;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;

/**
 * Created by Yoni on 3/18/2016.
 */
class GeneralAccessImpl implements GeneralAccess {

    final protected DataAccess dataAccess;
    final protected User currentUser;


    protected void requireItsMeForAccess(UserType userType, long userId) {
        if (currentUser.getUserType() != userType || currentUser.getId() != userId) {
            String messageText = "The current user ({0} {1}) has not access to manipulate other users ({2} {3}).";
            String message = MessageFormat.format(messageText, currentUser.getUserType(), currentUser.getId(), userType, userId);
            throw new IllegalArgumentException(message);
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
    public List<BookSupplier> findBooksOfSuppliers(User supplier) {
        return dataAccess.findEntityReferTo(BookSupplier.class, supplier);
    }



    @Override
    public <T extends Entity> T retrieve(Class<T> entityClass, long entityId) {
        return dataAccess.retrieve(entityClass, entityId);
    }

    @Override
    public User retrieveUserDetails() {
        if (currentUser.getUserType() != UserType.GUEST){
            return (User) dataAccess.retrieve(currentUser);
        } else {
            return (User) currentUser.clone();
        }
    }

    @Override
    public long upload(byte[] bytes) {
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setBytes(bytes);
        dataAccess.create(imageEntity);
        return imageEntity.getId();
    }

    @Override
    public void updateUserDetails(User newDetails) {
        requireItsMeForAccess(newDetails.getUserType(), newDetails.getId());
        newDetails.setCredentials(((User) dataAccess.retrieve(currentUser)).getCredentials()); // Avoid change credentials by this method.
        dataAccess.update(newDetails);
    }

    @Override
    public UserType getUserType() {
        return currentUser.getUserType();
    }

}
