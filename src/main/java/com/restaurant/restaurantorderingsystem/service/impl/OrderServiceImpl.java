package com.restaurant.restaurantorderingsystem.service.impl;

import com.restaurant.restaurantorderingsystem.dto.OrderItemRequest;
import com.restaurant.restaurantorderingsystem.dto.OrderRequest;
import com.restaurant.restaurantorderingsystem.dto.OrderResponse;
import com.restaurant.restaurantorderingsystem.entity.MenuItem;
import com.restaurant.restaurantorderingsystem.entity.Order;
import com.restaurant.restaurantorderingsystem.entity.OrderItem;
import com.restaurant.restaurantorderingsystem.entity.OrderStatus;
import com.restaurant.restaurantorderingsystem.entity.PaymentStatus;
import com.restaurant.restaurantorderingsystem.entity.User;
import com.restaurant.restaurantorderingsystem.exception.InsufficientStockException;
import com.restaurant.restaurantorderingsystem.exception.InvalidOrderException;
import com.restaurant.restaurantorderingsystem.exception.ResourceNotFoundException;
import com.restaurant.restaurantorderingsystem.repository.MenuItemRepository;
import com.restaurant.restaurantorderingsystem.repository.OrderRepository;
import com.restaurant.restaurantorderingsystem.service.OrderService;
import com.restaurant.restaurantorderingsystem.service.UserService;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OrderServiceImpl implements OrderService {

    private static final int MAX_LOCK_RETRIES = 3;
    private static final Set<OrderStatus> CANCELLABLE = EnumSet.of(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED
    );

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            MenuItemRepository menuItemRepository,
            UserService userService,
            TransactionTemplate transactionTemplate
    ) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public OrderResponse createOrder(Long customerId, OrderRequest request) {
        for (int attempt = 1; attempt <= MAX_LOCK_RETRIES; attempt++) {
            try {
                return transactionTemplate.execute(status -> createOrderInternal(customerId, request));
            } catch (OptimisticLockingFailureException ex) {
                if (attempt == MAX_LOCK_RETRIES) {
                    throw new InvalidOrderException("The menu was updated by another request. Please try again.");
                }
            }
        }
        throw new InvalidOrderException("The menu was updated by another request. Please try again.");
    }

    private OrderResponse createOrderInternal(Long customerId, OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }

        User customer = userService.getById(customerId);
        Map<Long, Integer> quantitiesByItem = mergeQuantities(request.getItems());

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantitiesByItem.entrySet()) {
            MenuItem menuItem = menuItemRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + entry.getKey()));
            int quantity = entry.getValue();
            validateAvailability(menuItem, quantity);

            BigDecimal unitPrice = menuItem.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);
            order.addItem(orderItem);

            menuItem.setAvailableQuantity(menuItem.getAvailableQuantity() - quantity);
            menuItemRepository.save(menuItem);
            total = total.add(subtotal);
        }

        order.setTotalPrice(total);
        return Mapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return Mapper.toOrderResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAllDetailed().stream().map(Mapper::toOrderResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByCustomer(Long customerId) {
        userService.getById(customerId);
        return orderRepository.findDetailedByCustomerId(customerId).stream().map(Mapper::toOrderResponse).toList();
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = getEntity(id);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("A cancelled order cannot change status");
        }
        if (order.getStatus() == OrderStatus.COMPLETED && status != OrderStatus.COMPLETED) {
            throw new InvalidOrderException("A completed order cannot change status");
        }
        if (status == OrderStatus.CANCELLED) {
            return cancel(id);
        }
        order.setStatus(status);
        if (status == OrderStatus.COMPLETED && order.getPaymentStatus() == PaymentStatus.PENDING) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        return Mapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancel(Long id) {
        Order order = getEntity(id);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderException("Order is already cancelled");
        }
        if (!CANCELLABLE.contains(order.getStatus())) {
            throw new InvalidOrderException("Order cannot be cancelled once it is " + order.getStatus());
        }
        for (OrderItem item : order.getItems()) {
            MenuItem menuItem = item.getMenuItem();
            menuItem.setAvailableQuantity(menuItem.getAvailableQuantity() + item.getQuantity());
            menuItemRepository.save(menuItem);
        }
        order.setStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        return Mapper.toOrderResponse(orderRepository.save(order));
    }

    private Order getEntity(Long id) {
        return orderRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    private Map<Long, Integer> mergeQuantities(List<OrderItemRequest> items) {
        Map<Long, Integer> merged = new HashMap<>();
        for (OrderItemRequest item : items) {
            if (item.getMenuItemId() == null) {
                throw new InvalidOrderException("Menu item id is required");
            }
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new InvalidOrderException("Quantity must be positive");
            }
            merged.merge(item.getMenuItemId(), item.getQuantity(), Integer::sum);
        }
        return merged;
    }

    private void validateAvailability(MenuItem menuItem, int quantity) {
        if (!menuItem.isActive()) {
            throw new InvalidOrderException("Menu item is not available: " + menuItem.getName());
        }
        if (menuItem.getAvailableQuantity() == null || menuItem.getAvailableQuantity() < quantity) {
            int available = menuItem.getAvailableQuantity() == null ? 0 : menuItem.getAvailableQuantity();
            throw new InsufficientStockException(
                    "Insufficient stock for " + menuItem.getName() + ". Requested " + quantity + ", available " + available
            );
        }
    }
}
