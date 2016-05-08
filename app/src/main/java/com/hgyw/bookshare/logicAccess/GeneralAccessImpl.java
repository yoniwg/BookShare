package hgyw.com.bookshare.logicAccess;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import hgyw.com.bookshare.dataAccess.DataAccess;
import hgyw.com.bookshare.entities.Book;
import hgyw.com.bookshare.entities.BookQuery;
import hgyw.com.bookshare.entities.BookReview;
import hgyw.com.bookshare.entities.BookSupplier;
import hgyw.com.bookshare.entities.Entity;
import hgyw.com.bookshare.entities.Supplier;
import hgyw.com.bookshare.entities.User;
import hgyw.com.bookshare.entities.UserType;

/**
 * Created by Yoni on 3/18/2016.
 */
class GeneralAccessImpl implements GeneralAccess {

    final protected DataAccess dataAccess;
    final private User currentUser;

    protected void requireItsMeForAccess(UserType userType, long userId) {
        if (currentUser.getUserType() != userType || currentUser.getId() != userId) {
            throw new IllegalArgumentException("The current user has not access to manipulate other users.");
        }
    }

    public GeneralAccessImpl(DataAccess dataAccess, User currentUser) {
        this.dataAccess = dataAccess;
        this.currentUser = currentUser;
    }

    @Override
    public List<Book> findBooks(BookQuery query) {
        return dataAccess.findBooks(query);
    }

    @Override
    public BigDecimal[] findBookPricesRange(Book book) {
        Collection<BigDecimal> prices = Stream.of(retrieveSuppliers(book))
                .map(BookSupplier::getPrice)
                .collect(Collectors.toList());
        if (prices.size() == 0) {
            return new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO};
        }
        return new BigDecimal[] {
                Stream.of(prices).min(BigDecimal::compareTo).get(),
                Stream.of(prices).max(BigDecimal::compareTo).get()
        };
    }

    @Override
    public List<Book> findSpecialOffers(int limit) {
        return dataAccess.findSpecialOffers(currentUser, limit);
    }

    @Override
    public Collection<BookReview> getBookReviews(Book book) {
        return dataAccess.findEntityReferTo(BookReview.class, book);
    }

    @Override
    public Collection<BookSupplier> retrieveSuppliers(Book book) {
        return dataAccess.findEntityReferTo(BookSupplier.class, book);
    }

    @Override
    public Collection<BookSupplier> retrieveBooksOfSuppliers(Supplier supplier) {
        return dataAccess.findEntityReferTo(BookSupplier.class, supplier);
    }

    @Override
    public <T extends Entity> T retrieve(Class<T> entityClass, long entityId) {
        return dataAccess.retrieve(entityClass, entityId);
    }

    public <T extends User> T retrieveUserDetails(T currentUser) {
        return (T) dataAccess.retrieve(currentUser);
    }

    public <T extends User> void updateUserDetails(T currentUser, T newDetails) {
        requireItsMeForAccess(newDetails.getUserType(), newDetails.getId());
        newDetails.setCredentials(((User) dataAccess.retrieve(currentUser)).getCredentials()); // Avoid change credentials by this method.
        dataAccess.update(newDetails);
    }

}
