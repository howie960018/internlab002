package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
