package com.hgyw.bookshare.entities;

import com.hgyw.bookshare.entities.reflection.EntityReference;

import java.math.BigDecimal;

/**
 * Created by Yoni on 3/15/2016.
 */
public class Order extends Entity {
    @EntityReference(BookSupplier.class)
    private long bookSupplierId;
    private int amount = 0;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private OrderRating orderRating = new OrderRating();
    private OrderStatus orderStatus = OrderStatus.NEW_ORDER;
    @EntityReference(Transaction.class)
    private long transactionId;

    
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal calcTotalPrice() {
        return getUnitPrice().multiply(BigDecimal.valueOf(getAmount()));
    }

    @Deprecated
    public long getBookSupplierId() {
        return bookSupplierId;
    }

    public void setBookSupplierId(long bookSupplierId) {
        this.bookSupplierId = bookSupplierId;
    }

    public OrderRating getOrderRating() {
        return orderRating;
    }

    public void setOrderRating(OrderRating orderRating) {
        this.orderRating = orderRating;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

}
