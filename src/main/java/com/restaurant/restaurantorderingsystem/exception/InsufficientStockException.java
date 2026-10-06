package com.restaurant.restaurantorderingsystem.exception;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
