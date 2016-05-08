package hgyw.com.bookshare.logicAccess;

import com.annimon.stream.Stream;

import java.util.Collection;
import java.util.Date;

import hgyw.com.bookshare.dataAccess.DataAccess;
import hgyw.com.bookshare.entities.Book;
import hgyw.com.bookshare.entities.BookReview;
import hgyw.com.bookshare.entities.BookSupplier;
import hgyw.com.bookshare.entities.Customer;
import hgyw.com.bookshare.entities.IdReference;
import hgyw.com.bookshare.entities.Order;
import hgyw.com.bookshare.entities.OrderRating;
import hgyw.com.bookshare.entities.OrderStatus;
import hgyw.com.bookshare.entities.Transaction;
import hgyw.com.bookshare.entities.UserType;
import hgyw.com.bookshare.exceptions.OrdersTransactionException;

/**
 * Created by haim7 on 20/03/2016.
 */
class CustomerAccessImpl extends GeneralAccessImpl implements CustomerAccess {

    final private Customer currentUser;

    public CustomerAccessImpl(DataAccess crud, Customer currentUser) {
        super(crud, currentUser);
        this.currentUser = currentUser;
    }

    @Override
    public Customer retrieveCustomerDetails() {
        return retrieveUserDetails(currentUser);
    }

    @Override
    public void updateCustomerDetails(Customer newDetails) {
        updateUserDetails(currentUser, newDetails);
    }

    @Override
    public Collection<BookReview> getCustomerReviews() {
        return dataAccess.findEntityReferTo(BookReview.class, currentUser);
    }

    @Override
    public Collection<Customer> findInterestedInBook(Book book) {
        return dataAccess.findInterestedInBook(book, currentUser);
    }

    @Override
    public Collection<Order> retrieveOrders(Date fromDate, Date toDate) {
        return dataAccess.retrieveOrders(currentUser, null, fromDate, toDate, false);
    }

    @Override
    public Collection<Order> retrieveActiveOrders() {
        return dataAccess.retrieveOrders(currentUser, null, null, null, true);
    }

    @Override
    public void performNewTransaction(Transaction transaction, Collection<Order> orders) throws OrdersTransactionException {
        for (Order order : orders) {
            BookSupplier bookSupplier = retrieve(BookSupplier.class, order.getBookSupplierId());
            order.setUnitPrice(bookSupplier.getPrice());
        }
        // validations and check that transaction can be done
        validateOrdersTransaction(orders);
        // create transaction
        transaction.setId(0);
        transaction.setDate(new Date());
        transaction.setCustomerId(retrieveCustomerDetails().getId());
        dataAccess.create(transaction);
        // create orders
        for (Order o : orders) {
            o.setId(0);
            o.setOrderStatus(OrderStatus.NEW);
            o.setTransactionId(transaction.getId());
            dataAccess.create(o);
            // decrease amount available
            BookSupplier bookSupplier = dataAccess.retrieve(BookSupplier.class, o.getBookSupplierId());
            bookSupplier.setAmountAvailable(bookSupplier.getAmountAvailable());
            dataAccess.update(bookSupplier);
        }
    }

    private void validateOrdersTransaction(Collection<Order> orders) throws OrdersTransactionException {
        for (Order o : orders) {
            // Validates according to bookSupplier in the database, the Order::getBookSupplierId() is
            //   only reference, and can be non updated.
            BookSupplier realBookSupplier = dataAccess.retrieve(BookSupplier.class, o.getBookSupplierId());
            if (!o.getUnitPrice().equals(realBookSupplier.getPrice())) {
                throw new OrdersTransactionException(OrdersTransactionException.Issue.PRICE_NOT_MATCH, o);
            }
            if (realBookSupplier.getAmountAvailable() <= 0) {
                throw new OrdersTransactionException(OrdersTransactionException.Issue.NOT_AVAILABLE, o);
            }
        }
    }

    @Override
    public void cancelOrder(long orderId) {
        Order order = dataAccess.retrieve(Order.class, orderId);
        Transaction transaction = retrieve(Transaction.class, order.getTransactionId());
        requireItsMeForAccess(UserType.CUSTOMER, transaction.getCustomerId());
        if (!(order.getOrderStatus() == OrderStatus.NEW
                || order.getOrderStatus() == OrderStatus.WAITING_FOR_PAYING)){
            throw new IllegalStateException("tc cancel the status must be " + OrderStatus.NEW + " or " + OrderStatus.WAITING_FOR_PAYING + ".");
        }
        order.setOrderStatus(OrderStatus.WAITING_FOR_CANCEL);
        dataAccess.update(order);
    }

    @Override
    public void updateOrderRating(long orderId, OrderRating orderRating) {
        Order order = dataAccess.retrieve(Order.class, orderId);
        Transaction transaction = retrieve(Transaction.class, order.getTransactionId());
        requireItsMeForAccess(UserType.CUSTOMER, transaction.getCustomerId());
        order.setOrderRating(orderRating);
        dataAccess.update(order);
    }

    @Override
    public void addBookReview(BookReview bookReview) {
        bookReview.setId(0);
        bookReview.setCustomerId(currentUser.getId());
        Book book = retrieve(Book.class, bookReview.getBookId());
        if (dataAccess.findEntityReferTo(BookReview.class, currentUser, book).size() > 0) {
            throw new IllegalStateException("The user already has review on this book!");
        }
        dataAccess.create(bookReview);
    }

    @Override
    public void updateBookReview(BookReview bookReview) {
        BookReview originalBookReview = (BookReview) dataAccess.retrieve(bookReview);
        requireItsMeForAccess(UserType.CUSTOMER, originalBookReview.getCustomerId());
        dataAccess.update(bookReview);
    }

    @Override
    public void removeBookReview(BookReview bookReview) {
        BookReview originalBookReview = (BookReview) dataAccess.retrieve(bookReview);
        requireItsMeForAccess(UserType.CUSTOMER, originalBookReview.getCustomerId());
        dataAccess.delete(bookReview);
    }

}
