package hgyw.com.bookshare.entities;

/**
 * Created by haim7 on 26/03/2016.
 */
public enum  OrderStatus {

    NEW, WAITING_FOR_PAYING, SENT, CLOSED, WAITING_FOR_CANCEL, CANCELED;

    /**
     * check if an instance is active one
     * @return - whether it is active order
     */
    public boolean isActive(){
        return this != CLOSED && this != CANCELED;
    }
}
