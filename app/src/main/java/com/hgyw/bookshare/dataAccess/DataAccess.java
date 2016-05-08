package hgyw.com.bookshare.dataAccess;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Function;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import hgyw.com.bookshare.entities.Book;
import hgyw.com.bookshare.entities.BookQuery;
import hgyw.com.bookshare.entities.BookSupplier;
import hgyw.com.bookshare.entities.Credentials;
import hgyw.com.bookshare.entities.Customer;
import hgyw.com.bookshare.entities.Entity;
import hgyw.com.bookshare.entities.IdReference;
import hgyw.com.bookshare.entities.Order;
import hgyw.com.bookshare.entities.Supplier;
import hgyw.com.bookshare.entities.User;

/**
 * Created by haim7 on 24/03/2016.
 */
public interface DataAccess extends Crud {

    /**
     * Retrieve user with credentials match the credentials parameters.
     * @param credentials The credentials to which we want match.
     * @return The user object (Customer or Supplier). null if it was found.
     */
    Optional<User> retrieveUserWithCredentials(Credentials credentials);

    /**
     * Return whether specific user name is taken,
     * @param username The username to check.
     * @return boolean value.
     */
    boolean isUsernameTaken(String username);

    /**
     * Finds another users that are interested in this book, according to user askedUser. (the
     * friends of askedUser for example.)
     * @param book the book in which interesting.
     * @param userAsked the user asked.
     * @return Collection of the interested.
     */
    Collection<Customer> findInterestedInBook(Book book, User userAsked);

    /**
     * Retrieve orders according to parameters are provided.
     * @param customer customer whose orders we wants.
     * @param supplier
     * @param fromDate Start date. null for ever.
     * @param toDate End date. null for ever.
     * @param onlyOpen boolean value that indicates whether to retrieve only open orders.
     * @return Collection of the results.
     */
    Collection<Order> retrieveOrders(Customer customer, Supplier supplier, Date fromDate, Date toDate, boolean onlyOpen);

    /**
     * Find books by book query.
     * @param query The Query
     * @return Collections of BookSupplier's. (the query could be depended on the supplier to.)
     */
    List<Book> findBooks(BookQuery query);

    /**
     * Find special offers to specified user.
      * @param user The user.
     * @param limit
     * @return Collection of BookSupplier's.
     */
    List<Book> findSpecialOffers(User user, int limit);


    /**
     * Find list of all items of referringClass that refer to referredItems.
     * @param <T>
     * @param referringClass Referring class
     * @param referredItems Item that referred by items.
     * @return Collection of referring items.
     * @throws java.util.NoSuchElementException if reffered items are not found.
     */
    <T extends Entity> Collection<T> findEntityReferTo(Class<T> referringClass, IdReference... referredItems);
}
