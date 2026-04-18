package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.AppUser;
import com.ctbc.assignment2.bean.CartItem;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.Order;
import com.ctbc.assignment2.bean.OrderItem;
import com.ctbc.assignment2.bean.OrderStatus;
import com.ctbc.assignment2.exception.DuplicateEnrollmentException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.AppUserRepository;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.OrderItemRepository;
import com.ctbc.assignment2.repository.OrderRepository;
import com.ctbc.assignment2.service.EnrollmentService;
import com.ctbc.assignment2.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private CourseBeanRepository courseRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Transactional
    @Override
    public Order createOrder(String username, List<CartItem> items) {
        AppUser buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0.0;
        for (CartItem item : items) {
            CourseBean course = courseRepository.findById(item.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + item.getCourseId()));
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setCourse(course);
            orderItem.setPrice(item.getPrice());
            orderItems.add(orderItem);
            total += item.getPrice();
        }
        order.setItems(orderItems);
        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    @Transactional
    @Override
    public Order pay(Long orderId, String username) {
        Order order = findById(orderId);
        if (!order.getBuyer().getUsername().equals(username)) {
            throw new IllegalStateException("Not your order");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not payable");
        }
        order.setStatus(OrderStatus.PAID);
        Order saved = orderRepository.save(order);
        for (OrderItem item : saved.getItems()) {
            try {
                enrollmentService.enroll(username, item.getCourse().getId());
            } catch (DuplicateEnrollmentException ex) {
                // Ignore duplicates from other enrollment paths.
            }
        }
        return saved;
    }

    @Transactional
    @Override
    public void cancel(Long orderId, String username) {
        Order order = findById(orderId);
        if (!order.getBuyer().getUsername().equals(username)) {
            throw new IllegalStateException("Not your order");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not cancellable");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    public List<Order> findByBuyer(String username) {
        return orderRepository.findByBuyerUsernameOrderByCreatedAtDesc(username);
    }

    @Override
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }
}
