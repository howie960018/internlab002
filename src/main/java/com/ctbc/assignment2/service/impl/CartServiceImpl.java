package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.CartItem;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseStatus;
import com.ctbc.assignment2.service.CartService;
import com.ctbc.assignment2.service.CourseBeanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private static final String CART_KEY = "CART";

    @Autowired
    private CourseBeanService courseService;

    @Override
    public List<CartItem> getCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_KEY, cart);
        }
        return cart;
    }

    @Override
    public void addToCart(HttpSession session, Long courseId) {
        List<CartItem> cart = getCart(session);
        for (CartItem item : cart) {
            if (item.getCourseId().equals(courseId)) {
                return;
            }
        }
        CourseBean course = courseService.findById(courseId);
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalStateException("Course is not available for enrollment");
        }
        cart.add(new CartItem(course.getId(), course.getCourseName(), course.getPrice()));
        session.setAttribute(CART_KEY, cart);
    }

    @Override
    public void removeFromCart(HttpSession session, Long courseId) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(item -> item.getCourseId().equals(courseId));
        session.setAttribute(CART_KEY, cart);
    }

    @Override
    public void clearCart(HttpSession session) {
        session.setAttribute(CART_KEY, new ArrayList<CartItem>());
    }

    @Override
    public double getTotal(HttpSession session) {
        List<CartItem> cart = getCart(session);
        double total = 0.0;
        for (CartItem item : cart) {
            if (item.getPrice() != null) {
                total += item.getPrice();
            }
        }
        return total;
    }
}
