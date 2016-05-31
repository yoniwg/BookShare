package com.hgyw.bookshare.logicAccess;

import com.annimon.stream.Optional;
import com.hgyw.bookshare.dataAccess.DataAccess;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.OrderStatus;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by haim7 on 26/03/2016.
 */
public class SupplierAccessImpl extends GeneralAccessImpl implements SupplierAccess {

    public SupplierAccessImpl(DataAccess crud, User currentUser) {
        super(crud, currentUser);
    }

    @Override
    public List<BookSupplier> retrieveMyBooks() {
        return findBooksOfSuppliers(currentUser);
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
    public List<Order> retrieveOrders(Date fromDate, Date toDate, boolean onlyActive) {
        return dataAccess.retrieveOrders(null, currentUser, fromDate, toDate, onlyActive);
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
        bookSupplier.setId(Entity.DEFAULT_ID);
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

    @Override
    public Optional<BookSupplier> retrieveMyBookSupplier(Book book) {
        List<BookSupplier> result = dataAccess.findEntityReferTo(BookSupplier.class, currentUser, book);
        if (!result.isEmpty()) {
            return Optional.of(result.get(0));
        } else {
            return Optional.empty();
        }
    }

}
