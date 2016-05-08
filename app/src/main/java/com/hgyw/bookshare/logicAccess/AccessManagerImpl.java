package hgyw.com.bookshare.logicAccess;

import hgyw.com.bookshare.dataAccess.DataAccessFactory;
import hgyw.com.bookshare.dataAccess.DataAccess;
import hgyw.com.bookshare.entities.Credentials;
import hgyw.com.bookshare.entities.Customer;
import hgyw.com.bookshare.entities.Guest;
import hgyw.com.bookshare.entities.Supplier;
import hgyw.com.bookshare.entities.User;
import hgyw.com.bookshare.entities.UserType;
import hgyw.com.bookshare.exceptions.WrongLoginException;

/**
 * Created by Yoni on 3/13/2016.
 */
enum AccessManagerImpl implements AccessManager {
    INSTANCE;

    private final User guest = new Guest();
    private final DataAccess crud = DataAccessFactory.getInstance();
    private GeneralAccess currentAccess;
    private User currentUser;

    AccessManagerImpl() {
        switchAccess(guest);
    }


    @Override
    public boolean isUserNameTaken(String username) {
        return crud.isUsernameTaken(username);
    }

    @Override
    public void signUp(User user) throws WrongLoginException {
        if (!(user.getUserType() == UserType.CUSTOMER || user.getUserType() == UserType.SUPPLIER)) {
            throw new IllegalArgumentException("The user should be instance of Customer or Supplier.");
        }
        if (user.getId() != 0) {
            throw new IllegalArgumentException("New item should have id 0.");
        }
        if (crud.isUsernameTaken(user.getCredentials().getUsername())) {
            throw new WrongLoginException(WrongLoginException.Issue.USERNAME_TAKEN);
        }
        crud.create(user);
        signIn(user.getCredentials());
    }

    @Override
    public void signIn(Credentials credentials) throws WrongLoginException {
        if (currentUser != guest) throw new IllegalStateException("There is a user that has already been signed in.");
        User newUser = crud.retrieveUserWithCredentials(credentials).orElseThrow(()->
            new WrongLoginException(WrongLoginException.Issue.WRONG_USERNAME_OR_PASSWORD)
        );
        switchAccess(newUser);
    }

    private void switchAccess(User newUser) {
        switch (newUser.getUserType()) {
            case GUEST:
                currentAccess = new GeneralAccessImpl(crud, newUser);
                break;
            case CUSTOMER:
                currentAccess = new CustomerAccessImpl(crud, (Customer) newUser);
                break;
            case SUPPLIER:
                currentAccess = new SupplierAccessImpl(crud, (Supplier) newUser);
                break;
        }
        currentUser = newUser;
    }

    @Override
    public void signOut() {
        if (currentAccess != guest) switchAccess(guest);
    }

    @Override
    public GeneralAccess getGeneralAccess() {
        return currentAccess;
    }

    @Override
    public CustomerAccess getCustomerAccess() {
        if (currentAccess instanceof CustomerAccess)
            return (CustomerAccess) currentAccess;
        throw new IllegalStateException("Customer user has not registered.");
    }

    @Override
    public SupplierAccess getSupplierAccess() {
        if (currentAccess instanceof SupplierAccess)
            return (SupplierAccess) currentAccess;
        throw new IllegalStateException("Supplier user has not registered.");
    }

    @Override
    public UserType getCurrentUserType() {
        return currentUser.getUserType();
    }

}
