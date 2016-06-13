package com.hgyw.bookshare.logicAccess;

import android.support.annotation.WorkerThread;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Yoni on 3/13/2016.
 */
public interface GeneralAccess {

    /**
     * Find books by query.
     * @param query The query
     * @return collections of BookSuppliers math the quary.
     */
    List<Book> findBooks(BookQuery query);

    /**
     * TODO documentation
     * @param book
     * @return
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
     * @param entityClass
     * @param entityId
     * @param <T>
     * @return
     */
    BigDecimal calcTotalPriceOfTransaction(Transaction transaction);

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
     * If userType is guest then return internal object of guest. Don't change it!
     * @return
     */
    User retrieveUserDetails();

    long upload(byte[] bytes);

    void updateUserDetails(User user);

    UserType getUserType();


}

