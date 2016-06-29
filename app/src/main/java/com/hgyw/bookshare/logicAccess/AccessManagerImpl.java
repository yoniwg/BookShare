package com.hgyw.bookshare.logicAccess;

import com.hgyw.bookshare.dataAccess.DataAccess;
import com.hgyw.bookshare.dataAccess.DataAccessFactory;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.exceptions.WrongLoginException;

/**
 * Singleton class, implements the AccessManager.
 */
enum AccessManagerImpl implements AccessManager {
    INSTANCE;

    // the guest user
    private final User guest = new User();
    {
        guest.setUserType(UserType.GUEST);
    }

    private final DataAccess dataAccess = DataAccessFactory.getInstance();

    private GeneralAccess currentAccess;
    private User currentUser;

    AccessManagerImpl() {
        switchAccess(guest);
    }


    @Override
    public boolean isUserNameTaken(String username) {
        return dataAccess.isUsernameTaken(username);
    }

    @Override
    public synchronized void  signUp(User user) throws WrongLoginException {
        if (!(user.getUserType() == UserType.CUSTOMER || user.getUserType() == UserType.SUPPLIER)) {
            throw new IllegalArgumentException("The user should be instance of Customer or Supplier.");
        }
        if (user.getId() != 0) {
            throw new IllegalArgumentException("New item should have id 0.");
        }
        if (user.getCredentials().getUsername().trim().isEmpty()) {
            throw new WrongLoginException(WrongLoginException.Issue.USERNAME_EMPTY);
        }
        if (dataAccess.isUsernameTaken(user.getCredentials().getUsername())) {
            throw new WrongLoginException(WrongLoginException.Issue.USERNAME_TAKEN);
        }
        dataAccess.create(user);
        signIn(user.getCredentials());
    }

    @Override
    public synchronized void signIn(Credentials credentials) throws WrongLoginException {
        if (currentUser != guest) throw new WrongLoginException(WrongLoginException.Issue.SOMEBODY_IS_ALREADY_SIGNED_IN);
        User newUser = dataAccess.retrieveUserWithCredentials(credentials).orElseThrow(()->
            new WrongLoginException(WrongLoginException.Issue.WRONG_USERNAME_OR_PASSWORD)
        );
        switchAccess(newUser);
    }

    /**
     * sets the user and the new access
     */
    private synchronized void switchAccess(User newUser) {
        switch (newUser.getUserType()) {
            case GUEST:
                currentAccess = new GeneralAccessImpl(dataAccess, newUser);
                break;
            case CUSTOMER:
                currentAccess = new CustomerAccessImpl(dataAccess, newUser);
                break;
            case SUPPLIER:
                currentAccess = new SupplierAccessImpl(dataAccess, newUser);
                break;
        }
        currentUser = newUser;
    }

    @Override
    public synchronized void signOut() {
        if (currentAccess != guest) switchAccess(guest);
    }

    @Override
    public synchronized GeneralAccess getGeneralAccess() {
        return currentAccess;
    }

    @Override
    public synchronized CustomerAccess getCustomerAccess() {
        if (currentAccess instanceof CustomerAccess) {
            return (CustomerAccess) currentAccess;
        }
        throw new IllegalStateException("Customer user has not registered.");
    }

    @Override
    public synchronized SupplierAccess getSupplierAccess() {
        if (currentAccess instanceof SupplierAccess) {
            return (SupplierAccess) currentAccess;
        }
        throw new IllegalStateException("Supplier user has not registered.");
    }

    @Override
    public synchronized UserType getCurrentUserType() {
        return currentUser.getUserType();
    }

}
