package com.hgyw.bookshare.logicAccess;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Order;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A class represents a cart of customer.
 * Created by Yoni on 5/12/2016.
 */
public class Cart implements Serializable {

    final private CustomerAccess access;

    private ArrayList<Long> ordersIdList = new ArrayList<>();

    public Cart(CustomerAccessImpl customerAccess) {
        access = customerAccess;
    }

    public void addToCart(long orderId){
        ordersIdList.add(orderId);
    }

    public void removeFromCart(long orderId){
        ordersIdList = Stream.of(ordersIdList).filter(o -> o != orderId).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Order> retrieveCartContent(){
        return Stream.of(ordersIdList).map(oid -> access.retrieve(Order.class, oid)).collect(Collectors.toList());
    }

    public void emptyCart(){
        ordersIdList = new ArrayList<>();
    }
}
