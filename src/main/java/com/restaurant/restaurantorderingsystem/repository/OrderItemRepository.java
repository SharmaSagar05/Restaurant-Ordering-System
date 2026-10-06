package com.restaurant.restaurantorderingsystem.repository;

import com.restaurant.restaurantorderingsystem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
