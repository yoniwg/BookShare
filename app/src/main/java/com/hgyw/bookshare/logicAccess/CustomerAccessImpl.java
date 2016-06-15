package com.hgyw.bookshare.logicAccess;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.dataAccess.DataAccess;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.OrderRating;
import com.hgyw.bookshare.entities.OrderStatus;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.exceptions.OrdersTransactionException;

/**
 * Created by haim7 on 20/03/2016.
 */
class CustomerAccessImpl extends GeneralAccessImpl implements CustomerAccess {

    final private Cart cart = new Cart();

    public CustomerAccessImpl(DataAccess crud, User currentUser) {
        super(crud, currentUser);
    }

    public void addBookSupplierToCart(BookSupplier bookSupplier, int amount) throws OrdersTransactionException {
        if (bookSupplier.getAmountAvailable() < amount){
            throw new OrdersTransactionException(OrdersTransactionException.Issue.NOT_AVAILABLE, null);
        }
        Order order = getCart().get(bookSupplier.getId());
        if (order == null){
            order = new Order();
            getCart().add(order);
            order.setUnitPrice(bookSupplier.getPrice());
            order.setBookSupplierId(bookSupplier.getId());
        }
        int globalAmount = order.getAmount() + amount;
        if (bookSupplier.getAmountAvailable() < globalAmount){
            throw new OrdersTransactionException(OrdersTransactionException.Issue.NOT_AVAILABLE, order);
        }
        order.setAmount(globalAmount);
    }

    @Override
    public List<Transaction> retrieveTransactions(Date fromDate, Date toDate) {
        return Stream.of(dataAccess.findEntityReferTo(Transaction.class, currentUser))
                .filter(t -> fromDate.compareTo(t.getDate()) <= 0 && toDate.compareTo(t.getDate()) >= 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> retrieveOrdersOfTransaction(Transaction transaction) {
        return dataAccess.findEntityReferTo(Order.class, transaction);
    }

    @Override
    public List<BookReview> getCustomerReviews() {
        return dataAccess.findEntityReferTo(BookReview.class, currentUser);
    }

    @Override
    public List<User> findInterestedInBook(Book book) {
        return dataAccess.findInterestedInBook(book, currentUser);
    }

    @Override
    public List<Order> retrieveOrders(Date fromDate, Date toDate) {
        return dataAccess.retrieveOrders(currentUser, null, fromDate, toDate, false);
    }

    @Override
    public List<Order> retrieveActiveOrders() {
        return dataAccess.retrieveOrders(currentUser, null, null, null, true);
    }

    @Override
    public Transaction performNewTransaction(Transaction transaction, Collection<Order> orders) throws OrdersTransactionException {
        // validations and check that transaction can be done
        validateOrdersTransaction(orders);
        // create transaction
        transaction.setId(Entity.DEFAULT_ID);
        transaction.setDate(new Timestamp(System.currentTimeMillis()));
        transaction.setCustomerId(retrieveUserDetails().getId());
        dataAccess.create(transaction);
        // create orders
        for (Order o : orders) {
            BookSupplier bookSupplier = dataAccess.retrieve(BookSupplier.class, o.getBookSupplierId());

            o.setId(Entity.DEFAULT_ID);
            o.setOrderStatus(OrderStatus.NEW_ORDER);
            o.setTransactionId(transaction.getId());
            o.setUnitPrice(bookSupplier.getPrice());
            dataAccess.create(o);

            // decrease amount available
            bookSupplier.setAmountAvailable(bookSupplier.getAmountAvailable() - o.getAmount());
            dataAccess.update(bookSupplier);
        }
        return transaction;
    }

    @Override
    public Transaction performNewTransaction() throws OrdersTransactionException {
        Transaction transaction = performNewTransaction(getCart().getTransaction(), getCart().retrieveCartContent());
        getCart().restartCart();
        return transaction;
    }

    private void validateOrdersTransaction(Collection<Order> orders) throws OrdersTransactionException {
        for (Order o : orders) {
            // Validates according to bookSupplier in the database, the Order::getBookSupplierId() is
            //   only reference, and can be non updated.
            BookSupplier realBookSupplier = dataAccess.retrieve(BookSupplier.class, o.getBookSupplierId());
//            if (!o.getUnitPrice().equals(realBookSupplier.getPrice())) {
//                throw new OrdersTransactionException(OrdersTransactionException.Issue.PRICE_NOT_MATCH, o);
//            }
            if (realBookSupplier.getAmountAvailable() < o.getAmount()) {
                throw new OrdersTransactionException(OrdersTransactionException.Issue.NOT_AVAILABLE, o);
            }
        }
    }

    @Override
    public void updateOrderStatus(Order currentOrder, OrderStatus newStatus) {
        Order order = dataAccess.retrieve(Order.class, currentOrder.getId());
        Transaction transaction = retrieve(Transaction.class, order.getTransactionId());
        requireItsMeForAccess(UserType.CUSTOMER, transaction.getCustomerId());
        if (!((order.getOrderStatus() == OrderStatus.NEW_ORDER && newStatus == OrderStatus.CANCELED)||
            (order.getOrderStatus() == OrderStatus.WAITING_FOR_PAYING
                && newStatus == OrderStatus.WAITING_FOR_CANCEL)||
            (order.getOrderStatus() == OrderStatus.SENT && newStatus == OrderStatus.CLOSED))) {
            throw new IllegalStateException("It is illegal to set "
                    + newStatus + " whether the current order status is " + order.getOrderStatus() + ".");
        }
        order.setOrderStatus(newStatus);
        dataAccess.update(order);
        currentOrder.setOrderStatus(newStatus);
        //return back amount
        if (newStatus == OrderStatus.CANCELED){
            BookSupplier bs = retrieve(BookSupplier.class, order.getBookSupplierId());
            bs.setAmountAvailable(bs.getAmountAvailable() + order.getAmount());
            dataAccess.update(bs);
        }
    }

    @Override
    public void updateOrderRating(Order currentOrder, OrderRating orderRating) {
        Order order = dataAccess.retrieve(currentOrder);
        Transaction transaction = retrieve(Transaction.class, order.getTransactionId());
        requireItsMeForAccess(UserType.CUSTOMER, transaction.getCustomerId());
        order.setOrderRating(orderRating);
        dataAccess.update(order);
        currentOrder.setOrderRating(orderRating);
    }

    @Override
    public void writeBookReview(BookReview bookReview) {

        List<BookReview> result = dataAccess.findEntityReferTo(BookReview.class, currentUser, IdReference.of(Book.class, bookReview.getBookId()));
        bookReview.setCustomerId(currentUser.getId());
        if (result.isEmpty()) {
            bookReview.setId(Entity.DEFAULT_ID);
            dataAccess.create(bookReview);
        } else {
            BookReview currentBookReview = result.get(0);
            bookReview.setId(currentBookReview.getId());
            dataAccess.update(bookReview);
        }

    }

    @Override
    public void removeBookReview(BookReview bookReview) {
        BookReview currentBookReview = dataAccess.retrieve(bookReview);
        requireItsMeForAccess(UserType.CUSTOMER, currentBookReview.getCustomerId());
        dataAccess.delete(bookReview);
    }

    @Override
    public BookReview retrieveMyReview(Book book) {
        List<BookReview> result = dataAccess.findEntityReferTo(BookReview.class, currentUser, book);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public Cart getCart() {
        return cart;
    }
}
