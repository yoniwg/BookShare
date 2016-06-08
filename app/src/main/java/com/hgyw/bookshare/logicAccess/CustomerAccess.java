package com.hgyw.bookshare.logicAccess;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.OrderRating;
import com.hgyw.bookshare.entities.OrderStatus;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.exceptions.OrdersTransactionException;

/**
 * Created by Yoni on 3/13/2016.
 */
public interface CustomerAccess extends GeneralAccess {

    /**
     * apply all customer's book reviews
     * @return the book reviews
     */
    Collection<BookReview> getCustomerReviews();

    /**
     * find the users which are interested in the provided book
     * @param book the book to be interested in
     * @return collection of interested users
     */
    Collection<User> findInterestedInBook(Book book);

    /**
     * retrieve all orders of current customer in a provided period
     * @param fromDate begin of period
     * @param toDate end of period
     * @return collection of orders
     */
    List<Order> retrieveOrders(Date fromDate, Date toDate);

    /**
     * retrieve all active orders of current customer (not closed or canceled)
     * @return collection of orders
     */
    Collection<Order> retrieveActiveOrders();

    /**
     * Make new transaction and associate collection of orders to it.
     * The price should be computed by Order::computePriceByBookSupplier().
     * The method call the Order::computePriceByBookSupplier().
     * The details that should set automatically in new transaction and orders, will set
     *  automatically. that is, the id will be ignored, and such transaction reference in orders,
     *  and order status will reset, and the date of transaction. the customer in transaction will
     *  set also to the current user.
     * The id's totally ignored' (as mention above), and will set to the new id.
     * @param transaction the transaction
     * @param orders collection of orders
     * @throws OrdersTransactionException for OrdersTransactionException issues. (see OrdersTransactionException.Issue)
     * @throws java.util.NoSuchElementException if the entities reference within the parameters entities are not found.
     * @see OrdersTransactionException.Issue for the issues
     */
    void performNewTransaction(Transaction transaction, Collection<Order> orders) throws OrdersTransactionException;

    /**
     * default call using cart
     */
    void performNewTransaction() throws OrdersTransactionException;

    /**
     * request to cancel the order
     * @param currentOrder the order to cancel
     * @param orderStatus
     * @throws java.util.NoSuchElementException if the order with id orderId is not found.
     * @throws IllegalStateException if current state of order does not allow canceling.
     */
    void updateOrderStatus(Order currentOrder, OrderStatus orderStatus);

    /**
     * update the order's rating
     * @param orderId the order to rate
     * @param orderRating the order's rating
     * @throws java.util.NoSuchElementException if the order with id orderId is not found.
     */
    void updateOrderRating(long orderId, OrderRating orderRating);


    /**
     * update review on book. the method ignores the bookReview.id, and set it to new Id if there is
     * no review of user and review.bookId, or to exists review (of user and book) if there is. </br>
     * The customerId will automatically set to the current user. </br>
     * @param bookReview the book review
     * @throws java.util.NoSuchElementException if the bookId is not found.
     */
    void writeBookReview(BookReview bookReview);

    /**
     * remove book review.
     * @param bookReview the book review
     * @throws java.util.NoSuchElementException if the bookReview is not fount.
     * @throws IllegalArgumentException if bookReview with such id is not of the current user.
     */
    void removeBookReview(BookReview bookReview);

    /**
     *
     * @param book the book
     * @return null if not fount
     */
    BookReview retrieveMyReview(Book book);

    /**
     * returns the cart of the customer
     * @return {@link Cart} - the cart
     */
    Cart getCart();

    /**
     * add book to cart by book supplier
     * @param bookSupplier
     * @param amount
     */
    void addBookSupplierToCart(BookSupplier bookSupplier, int amount);

    List<Transaction> retrieveTransactions(Date fromDate, Date toDate);

    List<Order> retrieveOrdersOfTransaction(Transaction transaction);
}
