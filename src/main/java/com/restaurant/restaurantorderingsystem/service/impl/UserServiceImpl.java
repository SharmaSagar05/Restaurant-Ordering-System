package com.restaurant.restaurantorderingsystem.service.impl;

import com.restaurant.restaurantorderingsystem.dto.RegisterRequest;
import com.restaurant.restaurantorderingsystem.dto.UserResponse;
import com.restaurant.restaurantorderingsystem.entity.Role;
import com.restaurant.restaurantorderingsystem.entity.User;
import com.restaurant.restaurantorderingsystem.exception.BusinessException;
import com.restaurant.restaurantorderingsystem.exception.ResourceNotFoundException;
import com.restaurant.restaurantorderingsystem.repository.UserRepository;
import com.restaurant.restaurantorderingsystem.service.UserService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessException("An account with this email already exists");
        }
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        return Mapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findCustomers() {
        return userRepository.findByRole(Role.CUSTOMER).stream().map(Mapper::toUserResponse).toList();
    }

    @Override
    public UserResponse toResponse(User user) {
        return Mapper.toUserResponse(user);
    }
}
