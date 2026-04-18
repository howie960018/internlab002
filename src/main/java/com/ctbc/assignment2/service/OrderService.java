package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.Order;
import com.ctbc.assignment2.bean.CartItem;

import java.util.List;

public interface OrderService {
    Order createOrder(String username, List<CartItem> items);

    Order pay(Long orderId, String username);

    void cancel(Long orderId, String username);

    List<Order> findByBuyer(String username);

    Order findById(Long orderId);
}
