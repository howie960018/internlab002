package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.CartItem;
import jakarta.servlet.http.HttpSession;
import java.util.List;

public interface CartService {
    List<CartItem> getCart(HttpSession session);
    void addToCart(HttpSession session, Long courseId);
    void removeFromCart(HttpSession session, Long courseId);
    void clearCart(HttpSession session);
    double getTotal(HttpSession session);
}
