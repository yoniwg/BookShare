package com.hgyw.bookshare.logicAccess;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class represents a cart of customer.
 * Created by Yoni on 5/12/2016.
 */
public class Cart implements Serializable {

    private List<Long> ordersIdList = new ArrayList<>();

    public void addToCart(long orderId){
        ordersIdList.add(orderId);
    }

    public void removeFromCart(long orderId){
        ordersIdList = Stream.of(ordersIdList).filter(o -> o != orderId).collect(Collectors.toList());
    }

    public List<Long> retrieveCartContent(){
        return ordersIdList;
    }

    public void emptyCart(){
        ordersIdList = new ArrayList<>();
    }
}
