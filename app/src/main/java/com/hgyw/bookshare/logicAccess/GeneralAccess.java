package com.hgyw.bookshare.logicAccess;

import android.support.annotation.WorkerThread;

import com.annimon.stream.Optional;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;

import java.math.BigDecimal;
import java.util.List;

/**
 *  * Logic access to general actions
 */
public interface GeneralAccess {

    /**
     * Find books by query.
     * @param query The query
     * @return collections of BookSuppliers math the quary.
     */
    List<Book> findBooks(BookQuery query);

    /**
     * Get {@link BookSummary} object for the book.
     * @param book the book
     * @return BookSummary contains data on the book.
     */
    BookSummary getBookSummary(Book book);

    /**
     * Find special offers for current user.
     * @param limit number of the offers are requested.
     * @return Collection of BookSuppliers.
     */
    List<Book> findSpecialOffers(int limit);

    /**
     *
     * @param book the book.
     * @return collection of BookReview.
     */
    List<BookReview> findBookReviews(Book book);

    /**
     * Retrieve BookSuppliers of book.
     * @param book the book
     * @return Collection of BookSuppliers.
     * @throws java.util.NoSuchElementException if the book is not found in database
     */
    List<BookSupplier> findBookSuppliers(Book book);

    /**
     * Retrieve Books of Suppliers.
     * @param supplier the supplier
     * @return Collection of BookSuppliers.
     * @throws java.util.NoSuchElementException if the supplier is not found in database
     */
    List<BookSupplier> findBooksOfSuppliers(User supplier);

    /**
     * Calaculate total price of transaction

     * @return
     */
    BigDecimal calcTotalPriceOfTransaction(Transaction transaction);

    /**
     * get all suppliers of a transaction
     */
    List<User> getSuppliersOfTransaction(Transaction transaction);

    /**
     * Retrieve any entity from data base
     * @param entityClass
     * @param entityId
     * @param <T>
     * @return
     */
    <T extends Entity> T retrieve(Class<T> entityClass, long entityId);

    /**
     * Retrieve any entity from data base if not DEFAULT ID
     * @param imageEntityClass
     * @param imageId
     * @return
     */
    <T extends Entity> Optional<T> retrieveOptional(Class<T> imageEntityClass, long imageId);
    /**
     * If userType is guest then return internal object of guest. Don't change it!
     * @return
     */
    User retrieveUserDetails();

    /**
     * Add new image to database
     * @return the id of image (the new ImageEntity item)
     */
    long upload(byte[] bytes);

    /**
     * update details of user
     * @param user the user
     */
    void updateUserDetails(User user);

    /**
     * @return the UserType of current user.
     */
    UserType getUserType();


}

