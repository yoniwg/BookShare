package com.hgyw.bookshare.logicAccess;

import java.util.Collection;
import java.util.Date;

import com.hgyw.bookshare.dataAccess.DataAccess;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.OrderStatus;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.entities.UserType;

/**
 * Created by haim7 on 26/03/2016.
 */
public class SupplierAccessImpl extends GeneralAccessImpl implements SupplierAccess {

    public SupplierAccessImpl(DataAccess crud, Supplier currentUser) {
        super(crud, currentUser);
    }

    @Override
    public Collection<BookSupplier> retrieveMyBooks() {
        return dataAccess.findEntityReferTo(BookSupplier.class, currentUser);
    }

    @Override
    public void addBook(Book book) {
        dataAccess.create(book);
    }

    @Override
    public void updateBook(Book book) {
        dataAccess.update(book);
    }

    @Override
    public Supplier retrieveSupplierDetails() {
        return (Supplier) retrieveUserDetails();
    }

    @Override
    public void updateSupplierDetails(Supplier newDetails) {
        updateUserDetails(newDetails);
    }

    @Override
    public Collection<Order> retrieveOrders(Date fromDate, Date toDate) {
        return dataAccess.retrieveOrders(null, (Supplier)currentUser, fromDate, toDate, false);
    }

    @Override
    public Collection<Order> retrieveActiveOrders(Date fromDate, Date toDate) {
        return dataAccess.retrieveOrders(null, (Supplier)currentUser, fromDate, toDate, true);
    }

    @Override
    public void updateOrderStatus(long orderId, OrderStatus orderStatus) {
        Order order = dataAccess.retrieve(Order.class, orderId);
        OrderStatus currentOrderStatus = order.getOrderStatus();
        BookSupplier bookSupplier = retrieve(BookSupplier.class, order.getBookSupplierId());
        requireItsMeForAccess(UserType.SUPPLIER, bookSupplier.getSupplierId());
        // the WAITING_FOR_CANCEL can set only by customer request.
        if (orderStatus == OrderStatus.WAITING_FOR_CANCEL) {
            throw new IllegalStateException("Supplier cannot set order state to waiting-for-cancel.");
        }
        // don't allow cance without customer request before.
        if (orderStatus == OrderStatus.CANCELED && currentOrderStatus != OrderStatus.WAITING_FOR_CANCEL) {
            throw new IllegalStateException("You cannot cancel non-waiting-for-cancel order");
        }
        order.setOrderStatus(orderStatus);
        dataAccess.update(order);
    }

    @Override
    public void addBookSupplier(BookSupplier bookSupplier) {
        bookSupplier.setId(0);
        bookSupplier.setSupplierId(currentUser.getId());
        IdReference book = retrieve(Book.class, bookSupplier.getBookId());
        Collection<BookSupplier> currentMatchedBookSuppliers = dataAccess.findEntityReferTo(BookSupplier.class, currentUser, book);
        if (currentMatchedBookSuppliers.size() > 0) {
            throw new IllegalStateException("The user already has bookSupplier on this book!");
        }
        dataAccess.create(bookSupplier);
    }

    @Override
    public void updateBookSupplier(BookSupplier bookSupplier) {
        BookSupplier originalBookSupplier = (BookSupplier) dataAccess.retrieve(bookSupplier);
        requireItsMeForAccess(UserType.SUPPLIER, originalBookSupplier.getSupplierId());
        dataAccess.update(bookSupplier);
    }

    @Override
    public void removeBookSupplier(BookSupplier bookSupplier) {
        BookSupplier originalBookSupplier = (BookSupplier) dataAccess.retrieve(bookSupplier);
        requireItsMeForAccess(UserType.SUPPLIER, originalBookSupplier.getSupplierId());
        dataAccess.delete(bookSupplier);
    }

}
