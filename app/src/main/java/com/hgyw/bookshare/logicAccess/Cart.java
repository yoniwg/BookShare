package com.hgyw.bookshare.logicAccess;

import com.hgyw.bookshare.entities.Order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A class represents a cart of customer.
 * Created by Yoni on 5/12/2016.
 */
public class Cart implements Serializable {

    private final List<Order> ordersList = new ArrayList<>();

    /**
     *
     * @param order
     * @return
     */
    public boolean add(Order order){
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

    public Order get(long bookSupplierId) {
        for (Order order : ordersList) {
            if (order.getBookSupplierId() == bookSupplierId) return order;
        }
        return null;
    }

    public List<Order> retrieveCartContent(){
        return ordersList;
    }

    public void clear(){
        ordersList.clear();
    }
}
