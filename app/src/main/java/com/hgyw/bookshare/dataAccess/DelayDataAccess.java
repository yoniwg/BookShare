package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.dataAccess.DataAccess;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.User;

import java.util.Date;
import java.util.List;

/**
 * Wrapper class for DataAccess that makes delay on each access to database.
 * (for development and debugging)
 */
class DelayDataAccess implements DataAccess {

    private final DataAccess dataAccess;
    private final long constructingTime = System.currentTimeMillis();

    public DelayDataAccess(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public <T extends IdReference> T retrieve(T idReference) {
        delay(idReference.getEntityType());
        return dataAccess.retrieve(idReference);
    }


    @Override
    public Optional<User> retrieveUserWithCredentials(Credentials credentials) {
        delay();
        return dataAccess.retrieveUserWithCredentials(credentials);
    }

    @Override
    public boolean isUsernameTaken(String username) {
        delay();
        return dataAccess.isUsernameTaken(username);
    }

    @Override
    public List<User> findInterestedInBook(Book book, User userAsked) {
        return delay(dataAccess.findInterestedInBook(book, userAsked));
    }

    @Override
    public List<Order> retrieveOrders(User customer, User supplier, Date fromDate, Date toDate, boolean onlyOpen) {
        return delay(dataAccess.retrieveOrders(customer, supplier, fromDate, toDate, onlyOpen));
    }

    @Override
    public List<Book> findBooks(BookQuery query) {
        return delay(dataAccess.findBooks(query));
    }

    @Override
    public List<Book> findSpecialOffers(User user, int limit) {
        return delay(dataAccess.findSpecialOffers(user, limit));
    }

    @Override
    public <T extends Entity> List<T> findEntityReferTo(Class<T> referringClass, IdReference... referredItems) {
        return delay(dataAccess.findEntityReferTo(referringClass, referredItems));
    }

    @Override
    public BookSummary getBookSummary(Book book) {
        delay();
        return dataAccess.getBookSummary(book);
    }

    @Override
    public void create(Entity item) {
        delay();
        dataAccess.create(item);
    }

    @Override
    public void update(Entity item) {
        delay();
        dataAccess.update(item);
    }

    @Override
    public void delete(IdReference item) {
        delay();
        dataAccess.delete(item);
    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityClass, long id) {
        delay(entityClass);
        return dataAccess.retrieve(entityClass, id);
    }


    private <T> List<T> delay(List<T> list) {
        delay(200 * (1 + list.size()));
        return list;
    }

    private void delay() {
        delay(200);
    }

    private void delay(Class<? extends Entity> entityClass) {
        delay(500);
    }

    int delayCounter = 0;
    private void delay(long millis) {
        if (System.currentTimeMillis() - constructingTime < 5*1000) return;
        delayCounter++;
        if (delayCounter % 10 == 0) System.out.println("delayCounter = " + delayCounter);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }


    public DataAccess getDataAccess() {
        return dataAccess;
    }
}
