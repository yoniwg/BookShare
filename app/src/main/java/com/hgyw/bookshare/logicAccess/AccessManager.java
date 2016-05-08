package hgyw.com.bookshare.logicAccess;

import hgyw.com.bookshare.entities.Credentials;
import hgyw.com.bookshare.entities.User;
import hgyw.com.bookshare.entities.UserType;
import hgyw.com.bookshare.exceptions.WrongLoginException;

/**
 * Interface for management of login to application.
 * The default connection is by guest user, and such is when the signOut() is called.
 */
public interface AccessManager {

    /**
     * Returns whether username is taken.
     * @param username The username to check.
     * @return boolean value.
     */
    boolean isUserNameTaken(String username);

    /**
     * Sign up new user. Auto sign in if the registration has succeed
     * @param user The user to sign up.
     * @throws IllegalArgumentException if user is not instance of Customer or Supplier. or user id is not 0.
     * @throws WrongLoginException if the username is taken.
     */
    void signUp(User user) throws WrongLoginException;

    /**
     * Sign in by exists account with the credentials.
     * @param credentials The credentials
     * @throws IllegalStateException if there is user signed in.
     * @throws WrongLoginException if the credentials don't match any user.
     */
    void signIn(Credentials credentials) throws WrongLoginException;

    /**
     * Sign out and set the access to guest access.
     * If no user signed out the method do nothing.
     */
    void signOut();

    /**
     * get general access for current user signed in.
     * @return GeneralAccess instance.
     */
    GeneralAccess getGeneralAccess();

    /**
     * Get customer access for customer signed in.
     * @throws IllegalStateException if the current user in not a customer.
     * @return CustomerAccess instance.
     */
    CustomerAccess getCustomerAccess();

    /**
     * Get supplier access for supplier signed in.
     * @throws IllegalStateException if the current user in not a supplier.
     * @return SupplierAccess instance.
     */
    SupplierAccess getSupplierAccess();

    /**
     * Get type of current user.
     * @return the type of the current user.
     */
    UserType getCurrentUserType();
}
