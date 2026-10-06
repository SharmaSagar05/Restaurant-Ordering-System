package com.restaurant.restaurantorderingsystem.controller.web;

import com.restaurant.restaurantorderingsystem.entity.OrderStatus;
import com.restaurant.restaurantorderingsystem.service.DashboardService;
import com.restaurant.restaurantorderingsystem.service.MenuItemService;
import com.restaurant.restaurantorderingsystem.service.OrderService;
import com.restaurant.restaurantorderingsystem.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminWebController {

    private final DashboardService dashboardService;
    private final MenuItemService menuItemService;
    private final OrderService orderService;
    private final UserService userService;

    public AdminWebController(
            DashboardService dashboardService,
            MenuItemService menuItemService,
            OrderService orderService,
            UserService userService
    ) {
        this.dashboardService = dashboardService;
        this.menuItemService = menuItemService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.getStats());
        model.addAttribute("menuItems", menuItemService.findAll());
        model.addAttribute("customers", userService.findCustomers());
        model.addAttribute("orders", orderService.findAll());
        model.addAttribute("statuses", OrderStatus.values());
        return "admin";
    }
}
