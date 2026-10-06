package com.restaurant.restaurantorderingsystem.dto;

import com.restaurant.restaurantorderingsystem.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class OrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
