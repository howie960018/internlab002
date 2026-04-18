package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerUsernameOrderByCreatedAtDesc(String username);
}
