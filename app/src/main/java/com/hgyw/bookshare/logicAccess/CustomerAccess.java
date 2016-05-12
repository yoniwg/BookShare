package com.hgyw.bookshare.logicAccess;

import java.util.Collection;
import java.util.Date;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.OrderRating;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.exceptions.OrdersTransactionException;

/**
 * Created by Yoni on 3/13/2016.
 */
public interface CustomerAccess extends GeneralAccess {

    /**
     * retrieve the customer's details
     * @return the details
     */
    Customer retrieveCustomerDetails();

    /**
     * replace the current customer's details with the provided details
     * @param newDetails - the new details
     * @throws IllegalArgumentException if the customer id does not match the current user.
     */
    void updateCustomerDetails(Customer newDetails);

    /**
     * get all customer's book reviews
     * @return the book reviews
     */
    Collection<BookReview> getCustomerReviews();

    /**
     * find the users which are interested in the provided book
     * @param book the book to be interested in
     * @return collection of interested users
     */
    Collection<Customer> findInterestedInBook(Book book);

    /**
     * retrieve all orders of current customer in a provided period
     * @param fromDate begin of period
     * @param toDate end of period
     * @return collection of orders
     */
    Collection<Order> retrieveOrders(Date fromDate, Date toDate);

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
     * request to cancel the order
     * @param orderId the order id to cancel
     * @throws java.util.NoSuchElementException if the order with id orderId is not found.
     * @throws IllegalStateException if current state of order does not allow canceling.
     */
    void cancelOrder(long orderId);

    /**
     * update the order's rating
     * @param orderId the order to rate
     * @param orderRating the order's rating
     * @throws java.util.NoSuchElementException if the order with id orderId is not found.
     */
    void updateOrderRating(long orderId, OrderRating orderRating);

    /**
     * Add new beautiful_list_item by current user for a book.
     * The BookReview.customer will set to current user, and id will be generated.
     * @param bookReview the new book beautiful_list_item
     * @throws IllegalStateException if current user already has beautiful_list_item on this book
     */
    void addBookReview(BookReview bookReview);

    /**
     * update exists beautiful_list_item.
     * @param bookReview the book beautiful_list_item
     * @throws IllegalArgumentException if this beautiful_list_item is not belong to current user.
     * @throws java.util.NoSuchElementException if beautiful_list_item is not found.
     */
    void updateBookReview(BookReview bookReview); // TODO: Problem with changing the referenced entity

    /**
     * remove a book beautiful_list_item
     * @param bookReview the beautiful_list_item with ID to delete
     * @throws IllegalArgumentException if the BookReview is not of current user.
     * @throws java.util.NoSuchElementException if the BookReview is not found in database.
     */
    void removeBookReview(BookReview bookReview);

    /**
     * returns the cart of the customer
     * @return {@link Cart} - the cart
     */
    Cart getCart();
}
