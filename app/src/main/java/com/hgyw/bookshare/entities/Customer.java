package hgyw.com.bookshare.entities;

/**
 * Created by Yoni on 3/15/2016.
 */
public class Customer extends User {

    @Override
    public UserType getUserType() {
        return UserType.CUSTOMER;
    }
}
