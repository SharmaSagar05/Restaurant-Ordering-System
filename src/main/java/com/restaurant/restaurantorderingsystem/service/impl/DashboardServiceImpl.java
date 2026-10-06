package com.restaurant.restaurantorderingsystem.service.impl;

import com.restaurant.restaurantorderingsystem.dto.DashboardStats;
import com.restaurant.restaurantorderingsystem.entity.OrderStatus;
import com.restaurant.restaurantorderingsystem.entity.PaymentStatus;
import com.restaurant.restaurantorderingsystem.entity.Role;
import com.restaurant.restaurantorderingsystem.repository.MenuItemRepository;
import com.restaurant.restaurantorderingsystem.repository.OrderRepository;
import com.restaurant.restaurantorderingsystem.repository.UserRepository;
import com.restaurant.restaurantorderingsystem.service.DashboardService;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public DashboardServiceImpl(
            MenuItemRepository menuItemRepository,
            UserRepository userRepository,
            OrderRepository orderRepository
    ) {
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalMenuItems(menuItemRepository.count());
        stats.setTotalCustomers(userRepository.countByRole(Role.CUSTOMER));
        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus(OrderStatus.PENDING));
        stats.setCompletedOrders(orderRepository.countByStatus(OrderStatus.COMPLETED));
        BigDecimal revenue = orderRepository.sumRevenue(PaymentStatus.PAID, OrderStatus.CANCELLED);
        stats.setTotalRevenue(revenue == null ? BigDecimal.ZERO : revenue);
        return stats;
    }
}
