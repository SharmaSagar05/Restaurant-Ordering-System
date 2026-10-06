package com.restaurant.restaurantorderingsystem.service;

import com.restaurant.restaurantorderingsystem.dto.RegisterRequest;
import com.restaurant.restaurantorderingsystem.dto.UserResponse;
import com.restaurant.restaurantorderingsystem.entity.User;
import java.util.List;

public interface UserService {

    UserResponse register(RegisterRequest request);

    User getById(Long id);

    User getByEmail(String email);

    List<UserResponse> findCustomers();

    UserResponse toResponse(User user);
}
