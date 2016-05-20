package com.hgyw.bookshare.entities;

import com.hgyw.bookshare.entities.reflection.EntityReference;

import java.util.Date;

/**
 * Created by Yoni on 3/15/2016.
 */
public class Transaction extends Entity {

    @EntityReference(User.class)
    private long customerId;
    private Date date = new Date();
    private String creditCard = "";
    private String shippingAddress = "";

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

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
