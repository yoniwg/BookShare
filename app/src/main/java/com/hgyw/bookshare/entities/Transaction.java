package hgyw.com.bookshare.entities;

import java.util.Date;

/**
 * Created by Yoni on 3/15/2016.
 */
public class Transaction extends Entity {

    @EntityReference(Customer.class)
    private long customerId;
    private Date date = new Date();
    private String creditCard = "";

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }
}
