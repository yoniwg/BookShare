package com.hgyw.bookshare.logicAccess;

import java.util.Date;
import java.util.List;

import com.annimon.stream.Optional;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.OrderStatus;

/**
 * Created by Yoni on 3/13/2016.
 */
public interface SupplierAccess extends GeneralAccess {

    /**
     * retrieve books of current supplier user
     * @return collection of BookSupplier
     */
    List<BookSupplier> retrieveMyBooks();

    /**
     * Add book to database.
     * @param book the book. the id will set to generated id.
     * @throws IllegalArgumentException if the book id is not 0.
     */
    void addBook(Book book);

    /**
     * Update book in database.
     * @param book the book.
     * @throws java.util.NoSuchElementException if a book with such id is not found.
     */
    void updateBook(Book book);

    /**
     * retrieve all orders from current supplier in a provided period
     * @param fromDate begin of period
     * @param toDate end of period
     * @param onlyActive whether to retrieve only active orders
     * @return collection of orders
     */
    List<Order> retrieveOrders(Date fromDate, Date toDate, boolean onlyActive);

    /**
     * update the order status
     * @param currentOrder the id of order
     * @param orderStatus the new status
     * @throws java.util.NoSuchElementException if the order with id orderId is not found.
     * @throws IllegalStateException if current status of order is not match the new (according to implementation).
     */
    void updateOrderStatus(Order currentOrder, OrderStatus orderStatus);

    /**
     * Add new BookSupplier by current user for a book.
     * The BookSupplier.supplier will set to current user, and id will be generated.
     * @param bookSupplier the new BookSupplier
     * @throws IllegalStateException if current user already has BookSupplier on this book
     */
    void addBookSupplier(BookSupplier bookSupplier);

    /**
     * update exists BookSupplier.
     * @param bookSupplier the BookSupplier
     * @throws IllegalArgumentException if this BookSupplier is not belong to current user.
     * @throws java.util.NoSuchElementException if BookSupplier is not found.
     */
    void updateBookSupplier(BookSupplier bookSupplier);  // TODO: Problem with changing the referenced entity

    /**
     * remove a BookSupplier
     * @param bookSupplier the BookSupplier with ID to delete
     * @throws IllegalArgumentException if the BookSupplier is not of current supplier user.
     * @throws java.util.NoSuchElementException if the BookSupplier is not found in database.
     */
    void removeBookSupplier(BookSupplier bookSupplier);

    Optional<BookSupplier> retrieveMyBookSupplier(Book book);
}
