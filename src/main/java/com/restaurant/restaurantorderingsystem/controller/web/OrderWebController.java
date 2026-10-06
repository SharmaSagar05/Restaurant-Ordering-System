package com.restaurant.restaurantorderingsystem.controller.web;

import com.restaurant.restaurantorderingsystem.dto.OrderItemRequest;
import com.restaurant.restaurantorderingsystem.dto.OrderRequest;
import com.restaurant.restaurantorderingsystem.dto.OrderResponse;
import com.restaurant.restaurantorderingsystem.entity.OrderStatus;
import com.restaurant.restaurantorderingsystem.entity.Role;
import com.restaurant.restaurantorderingsystem.entity.User;
import com.restaurant.restaurantorderingsystem.exception.BusinessException;
import com.restaurant.restaurantorderingsystem.service.MenuItemService;
import com.restaurant.restaurantorderingsystem.service.OrderService;
import com.restaurant.restaurantorderingsystem.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderWebController {

    private final OrderService orderService;
    private final MenuItemService menuItemService;
    private final UserService userService;

    public OrderWebController(OrderService orderService, MenuItemService menuItemService, UserService userService) {
        this.orderService = orderService;
        this.menuItemService = menuItemService;
        this.userService = userService;
    }

    @GetMapping("/order/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String createForm(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("menuItems", menuItemService.search(q, true));
        model.addAttribute("q", q);
        return "order-create";
    }

    @PostMapping("/order/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String create(
            Authentication authentication,
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes
    ) {
        User customer = userService.getByEmail(authentication.getName());
        OrderRequest request = new OrderRequest();
        List<OrderItemRequest> items = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().startsWith("qty_")) {
                continue;
            }
            int quantity = parseQuantity(entry.getValue());
            if (quantity < 1) {
                continue;
            }
            OrderItemRequest item = new OrderItemRequest();
            item.setMenuItemId(Long.parseLong(entry.getKey().substring(4)));
            item.setQuantity(quantity);
            items.add(item);
        }
        request.setItems(items);
        OrderResponse order = orderService.createOrder(customer.getId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Order placed successfully. Total ₹" + order.getTotalPrice());
        return "redirect:/orders/" + order.getId();
    }

    @GetMapping("/orders")
    public String list(Authentication authentication, Model model) {
        User current = userService.getByEmail(authentication.getName());
        if (current.getRole() == Role.CUSTOMER) {
            model.addAttribute("orders", orderService.findByCustomer(current.getId()));
        } else {
            model.addAttribute("orders", orderService.findAll());
        }
        model.addAttribute("statuses", OrderStatus.values());
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String details(@PathVariable Long id, Authentication authentication, Model model) {
        User current = userService.getByEmail(authentication.getName());
        OrderResponse order = orderService.getById(id);
        if (current.getRole() == Role.CUSTOMER && !current.getId().equals(order.getCustomerId())) {
            throw new BusinessException("You can only view your own orders");
        }
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("cancellable", order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED);
        return "order-details";
    }

    @PostMapping("/orders/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public String status(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            RedirectAttributes redirectAttributes
    ) {
        orderService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + status);
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        User current = userService.getByEmail(authentication.getName());
        OrderResponse order = orderService.getById(id);
        if (current.getRole() == Role.CUSTOMER && !current.getId().equals(order.getCustomerId())) {
            throw new BusinessException("You can only cancel your own orders");
        }
        orderService.cancel(id);
        redirectAttributes.addFlashAttribute("successMessage", "Order cancelled and inventory restored.");
        return "redirect:/orders/" + id;
    }

    private int parseQuantity(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
