package com.hgyw.bookshare.logicAccess;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Transaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * A class represents a cart of customer.
 */
public class Cart implements Serializable {

    private final List<Order> ordersList = new ArrayList<>();
    private final Transaction transaction = new Transaction();

    public Cart(){
        restartCart();
    }

    /**
     * add order to cart. if the cart has order with the same bookSupplierId it will sum the amount.
     * @return true if new order added, false if the sum only was updated.
     * @throws NullPointerException if order == null
     */
    public boolean add(Order order){
        Objects.requireNonNull(order);
        Order currentSameOrder = get(order.getBookSupplierId());
        if (currentSameOrder == null) {
            ordersList.add(order);
            return true;
        }
        else {
            currentSameOrder.setAmount(currentSameOrder.getAmount() + order.getAmount());
            return false;
        }
    }

    /**
     * remove order of refer to bookSupplierId
     * @return false if this bookSupplierId is not in the cart, true otherwise.
     */
    public boolean remove(long bookSupplierId){
        for (ListIterator<Order> it = ordersList.listIterator(); it.hasNext();){
            Order order = it.next();
            if (order.getBookSupplierId() == bookSupplierId) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * @return the order refer to bookSupplierId. null if not found.
     */
    public Order get(long bookSupplierId) {
        for (Order order : ordersList) {
            if (order.getBookSupplierId() == bookSupplierId) return order;
        }
        return null;
    }

    /**
     * Returns unmodifiable list of orders of this cart, backed by this cart (adding to cart will
     * adds to list, for instance).
     */
    public List<Order> retrieveCartContent(){
        return Collections.unmodifiableList(ordersList);
    }

    /**
     * clear the cart
     */
    public void clear(){
        ordersList.clear();
    }

    /**
     * @return whether thw cart is empty
     */
    public boolean isEmpty(){
        return ordersList.isEmpty();
    }

    /**
     * @return The transaction in this cart.
     */
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * Set transaction details
     */
    public void setTransactionDetails(String shippingAddress, String creditNumber){
        transaction.setShippingAddress(shippingAddress);
        transaction.setCreditCard(creditNumber);
    }

    /**
     * clear the cart, and restart the transaction details
     */
    public void restartCart(){
        clear();
        setTransactionDetails("", "");
    }

    /**
     * Calculate total price of all cart's orders.
     */
    public BigDecimal calculateTotalSum(){
        return Stream.of(ordersList)
                .map(Order::calcTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
