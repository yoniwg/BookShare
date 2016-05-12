package com.hgyw.bookshare.logicAccess;

import com.hgyw.bookshare.entities.Order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class represents a cart of customer.
 * Created by Yoni on 5/12/2016.
 */
public class Cart implements Serializable {

    private final List<Order> ordersList = new ArrayList<>();

    public void addToCart(Order order){
        ordersList.add(order);
    }

    public void removeFromCart(Order order){
        ordersList.remove(order);
    }

    public List<Order> retrieveCartContent(){
        return ordersList;
    }

    public void clearCart(){
        ordersList.clear();
    }
}
