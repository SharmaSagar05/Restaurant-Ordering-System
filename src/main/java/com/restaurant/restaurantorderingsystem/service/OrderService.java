package com.restaurant.restaurantorderingsystem.service;

import com.restaurant.restaurantorderingsystem.dto.DashboardStats;
import com.restaurant.restaurantorderingsystem.dto.OrderRequest;
import com.restaurant.restaurantorderingsystem.dto.OrderResponse;
import com.restaurant.restaurantorderingsystem.entity.OrderStatus;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(Long customerId, OrderRequest request);

    OrderResponse getById(Long id);

    List<OrderResponse> findAll();

    List<OrderResponse> findByCustomer(Long customerId);

    OrderResponse updateStatus(Long id, OrderStatus status);

    OrderResponse cancel(Long id);
}
