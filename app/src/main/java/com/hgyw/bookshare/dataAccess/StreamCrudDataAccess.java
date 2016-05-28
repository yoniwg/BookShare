package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.Predicate;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Rating;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.reflection.EntityReflection;

/**
 * Created by haim7 on 23/03/2016.
 */
class StreamCrudDataAccess implements DataAccess {

    private final StreamCrud crud;

    StreamCrudDataAccess(StreamCrud crud) {
        this.crud = crud;
    }

    ///////////////////////////
    // Delegate Methods
    //////////////////////////

    @Override
    public void create(Entity item) {
        crud.create(item);
    }

    @Override
    public void update(Entity item) {
        crud.update(item);
    }

    @Override
    public void delete(IdReference item) {
        crud.delete(item);
    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityClass, long id) {
        return crud.retrieve(entityClass, id);
    }

    ///////////////////////////
    // Delegate Methods End
    //////////////////////////

    @Override
    public Optional<User> retrieveUserWithCredentials(Credentials credentials) {
        return streamAllUsers().filter(u -> u.getCredentials().equals(credentials))
                .findFirst();
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return streamAllUsers()
                .anyMatch(u -> u.getCredentials().getUsername().equals(username));
    }

    private Stream<User> streamAllUsers() {
        return streamAllNonDeleted(User.class);
    }

    @Override
    public List<User> findInterestedInBook(Book book, User userAsked) {
        return streamAllNonDeleted(Order.class)
                .filter(o -> retrieve(BookSupplier.class, o.getBookSupplierId()).getBookId() == book.getId())
                .map(retrieving(Transaction.class, Order::getTransactionId))
                .map(retrieving(User.class, Transaction::getCustomerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> retrieveOrders(User customer, User supplier, Date fromDate, Date toDate, boolean onlyOpen) {
        return streamAllNonDeleted(Order.class)
                .filter(o -> {
                    Transaction transaction = retrieve(Transaction.class, o.getTransactionId());
                    BookSupplier bookSupplier = retrieve(BookSupplier.class, o.getBookSupplierId());
                    return (customer == null || transaction.getCustomerId() == customer.getId())
                                    && (supplier == null || bookSupplier.getSupplierId() == supplier.getId())
                                    && isBetween(transaction.getDate(), fromDate, toDate)
                                    && (!onlyOpen || o.getOrderStatus().isActive());
                }).collect(Collectors.toList());
    }

    @Override
    public List<Book> findBooks(BookQuery query) {
        return streamAllNonDeleted(Book.class)
                .filter(book -> performFilterQuery(book, query))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findSpecialOffers(User user, int limit) {
        List<String> topAuthors = getTopInstances(getDistinctBooksOfUser(user)
                .map(retrieving(Book.class, BookSupplier::getBookId))
                .map(Book::getAuthor), limit
        );
        List<Book.Genre> topGenre = getTopInstances(getDistinctBooksOfUser(user)
                .map(retrieving(Book.class, BookSupplier::getBookId))
                .map(Book::getGenre), limit
        );
        //find books from top authors and genres
        // give high priority to author & genre fitness
        Function<Book, Integer> rateValueOfBook = book -> -(
                (topAuthors.contains(book.getAuthor()) ? 1 : 0)
                + (topGenre.contains(book.getGenre()) ? 1 : 0)
        );
        return streamAllNonDeleted(Book.class)
                .sortBy(rateValueOfBook)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Stream<BookSupplier> getDistinctBooksOfUser(User currentUser) {
        return streamAllNonDeleted(Order.class)
                .filter(o -> retrieve(Transaction.class, o.getTransactionId()).getCustomerId() == currentUser.getId())
                .map(retrieving(BookSupplier.class, Order::getBookSupplierId))
                .distinct();
    }

    private <T> List<T> getTopInstances(Stream<T> stream, int amount){
        Map<T, Integer> map = new HashMap<>();
        for (T t: stream.collect(Collectors.toList())) {
            Integer oldValue = map.get(t);
            map.put(t, (oldValue == null) ? 1 : oldValue + 1);
        }
        return Stream.of(map)
                .sortBy(Map.Entry::getValue)
                .map(Map.Entry::getKey).limit(amount).collect(Collectors.toList());
    }


    @Override
    public <T extends Entity> List<T> findEntityReferTo(Class<T> referringClass, IdReference ... referredItems) {
        Predicate<T> predicate = EntityReflection.predicateEntityReferTo(referringClass, referredItems);
        return streamAllNonDeleted(referringClass).filter(predicate).collect(Collectors.toList());
    }

    @Override
    public BookSummary getBookSummary(Book book) {
        BookSummary bookSummary = new BookSummary();
        Collection<BigDecimal> prices = streamAllNonDeleted(BookSupplier.class)
                .filter(bs -> bs.getBookId() == book.getId())
                .map(BookSupplier::getPrice)
                .collect(Collectors.toList());
        if (prices.size() != 0) {
            bookSummary.setMinPrice(Collections.min(prices));
            bookSummary.setMaxPrice(Collections.max(prices));
        }
        Map<Rating, Integer> ratingMap = streamAllNonDeleted(BookReview.class)
                .filter(review -> review.getBookId() == book.getId())
                .collect(Collectors.groupingBy(BookReview::getRating,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        bookSummary.setRatingMap(ratingMap);
        return bookSummary;
    }

    public <T extends Entity> Stream<T> streamAllNonDeleted(Class<T> entityType) {
        return crud.streamAll(entityType).filter(e -> !e.isDeleted());
    }

    private boolean performFilterQuery(Book book, BookQuery bookQuery) {
        BigDecimal price = streamAllNonDeleted(BookSupplier.class)
                .filter(bs -> bs.getBookId() == book.getId())
                .map(BookSupplier::getPrice)
                .max(BigDecimal::compareTo)
                .orElse(null);
        if (price == null) return false;
        return book.getTitle().toLowerCase().contains(bookQuery.getTitleQuery().toLowerCase())
                && book.getAuthor().toLowerCase().contains(bookQuery.getAuthorQuery().toLowerCase())
                && (bookQuery.getGenreSet() == null || bookQuery.getGenreSet().contains(book.getGenre()))
                && isBetween(price, bookQuery.getBeginPrice(), bookQuery.getEndPrice());
    }

    private static <T extends Comparable<T>> boolean isBetween(T value, T fromValue, T toValue) {
        return (fromValue==null || value.compareTo(fromValue) >= 0)
                && (toValue==null || value.compareTo(toValue) < 0);
    }

    @Override
    public Entity retrieve(IdReference idReference) {
        return retrieve(idReference.getEntityType(), idReference.getId());
    }

    public <T extends Entity, R extends Entity> Function<T, R> retrieving(Class<R> referredClass, Function<T, Long> referenceFunction) {
        return t -> retrieve(referredClass, referenceFunction.apply(t));
    }

}
