package com.restaurant.restaurantorderingsystem.service.impl;

import com.restaurant.restaurantorderingsystem.dto.MenuItemResponse;
import com.restaurant.restaurantorderingsystem.dto.OrderItemResponse;
import com.restaurant.restaurantorderingsystem.dto.OrderResponse;
import com.restaurant.restaurantorderingsystem.dto.UserResponse;
import com.restaurant.restaurantorderingsystem.entity.MenuItem;
import com.restaurant.restaurantorderingsystem.entity.Order;
import com.restaurant.restaurantorderingsystem.entity.OrderItem;
import com.restaurant.restaurantorderingsystem.entity.User;
import java.util.List;

public final class Mapper {

    private Mapper() {
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }

    public static MenuItemResponse toMenuItemResponse(MenuItem item) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setPrice(item.getPrice());
        response.setAvailableQuantity(item.getAvailableQuantity());
        response.setActive(item.isActive());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        response.setVersion(item.getVersion());
        return response;
    }

    public static OrderItemResponse toOrderItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setMenuItemId(item.getMenuItem().getId());
        response.setMenuItemName(item.getMenuItem().getName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setSubtotal(item.getSubtotal());
        return response;
    }

    public static OrderResponse toOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomer().getId());
        response.setCustomerName(order.getCustomer().getName());
        response.setCustomerEmail(order.getCustomer().getEmail());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setItems(order.getItems() == null ? List.of() : order.getItems().stream().map(Mapper::toOrderItemResponse).toList());
        return response;
    }
}
