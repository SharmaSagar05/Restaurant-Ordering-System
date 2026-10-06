package com.restaurant.restaurantorderingsystem.controller.api;

import com.restaurant.restaurantorderingsystem.dto.OrderRequest;
import com.restaurant.restaurantorderingsystem.dto.OrderResponse;
import com.restaurant.restaurantorderingsystem.dto.OrderStatusRequest;
import com.restaurant.restaurantorderingsystem.entity.Role;
import com.restaurant.restaurantorderingsystem.entity.User;
import com.restaurant.restaurantorderingsystem.exception.BusinessException;
import com.restaurant.restaurantorderingsystem.service.OrderService;
import com.restaurant.restaurantorderingsystem.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderRestController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> list(Authentication authentication) {
        User current = currentUser(authentication);
        if (current.getRole() == Role.CUSTOMER) {
            return ResponseEntity.ok(orderService.findByCustomer(current.getId()));
        }
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get(@PathVariable Long id, Authentication authentication) {
        OrderResponse order = orderService.getById(id);
        assertCanView(currentUser(authentication), order);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> byCustomer(@PathVariable Long customerId, Authentication authentication) {
        User current = currentUser(authentication);
        if (current.getRole() == Role.CUSTOMER && !current.getId().equals(customerId)) {
            throw new BusinessException("You can only view your own orders");
        }
        return ResponseEntity.ok(orderService.findByCustomer(customerId));
    }

    @PostMapping("/customer/{customerId}")
    public ResponseEntity<OrderResponse> create(
            @PathVariable Long customerId,
            @Valid @RequestBody OrderRequest request,
            Authentication authentication
    ) {
        User current = currentUser(authentication);
        if (current.getRole() == Role.CUSTOMER && !current.getId().equals(customerId)) {
            throw new BusinessException("You can only place orders for your own account");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(customerId, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> status(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request,
            Authentication authentication
    ) {
        User current = currentUser(authentication);
        if (current.getRole() == Role.CUSTOMER) {
            throw new BusinessException("Customers cannot update order status");
        }
        return ResponseEntity.ok(orderService.updateStatus(id, request.getStatus()));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id, Authentication authentication) {
        OrderResponse order = orderService.getById(id);
        assertCanView(currentUser(authentication), order);
        return ResponseEntity.ok(orderService.cancel(id));
    }

    private User currentUser(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }

    private void assertCanView(User current, OrderResponse order) {
        if (current.getRole() == Role.CUSTOMER && !current.getId().equals(order.getCustomerId())) {
            throw new BusinessException("You can only access your own orders");
        }
    }
}
