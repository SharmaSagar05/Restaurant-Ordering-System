package com.restaurant.restaurantorderingsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.restaurant.restaurantorderingsystem.dto.MenuItemRequest;
import com.restaurant.restaurantorderingsystem.dto.MenuItemResponse;
import com.restaurant.restaurantorderingsystem.dto.OrderItemRequest;
import com.restaurant.restaurantorderingsystem.dto.OrderRequest;
import com.restaurant.restaurantorderingsystem.dto.OrderResponse;
import com.restaurant.restaurantorderingsystem.dto.RegisterRequest;
import com.restaurant.restaurantorderingsystem.entity.OrderStatus;
import com.restaurant.restaurantorderingsystem.exception.InsufficientStockException;
import com.restaurant.restaurantorderingsystem.exception.InvalidOrderException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private MenuItemService menuItemService;
    @Autowired
    private UserService userService;

    private Long customerId;
    private Long pizzaId;
    private Long burgerId;

    @BeforeEach
    void setUp() {
        RegisterRequest register = new RegisterRequest();
        register.setName("Test Customer");
        register.setEmail("order-test@restaurant.com");
        register.setPassword("Customer1");
        customerId = userService.register(register).getId();

        pizzaId = createMenu("Pizza", "250.00", 20).getId();
        burgerId = createMenu("Burger", "150.00", 10).getId();
    }

    @Test
    void createOrderCalculatesSubtotalsAndTotal() {
        OrderResponse order = orderService.createOrder(customerId, request(
                item(pizzaId, 2),
                item(burgerId, 1)
        ));

        assertThat(order.getTotalPrice()).isEqualByComparingTo("650.00");
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getItems())
                .anySatisfy(line -> {
                    if ("Pizza".equals(line.getMenuItemName())) {
                        assertThat(line.getUnitPrice()).isEqualByComparingTo("250.00");
                        assertThat(line.getQuantity()).isEqualTo(2);
                        assertThat(line.getSubtotal()).isEqualByComparingTo("500.00");
                    }
                });
        assertThat(menuItemService.getById(pizzaId).getAvailableQuantity()).isEqualTo(18);
        assertThat(menuItemService.getById(burgerId).getAvailableQuantity()).isEqualTo(9);
    }

    @Test
    void rejectsInsufficientStock() {
        assertThatThrownBy(() -> orderService.createOrder(customerId, request(item(burgerId, 99))))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(menuItemService.getById(burgerId).getAvailableQuantity()).isEqualTo(10);
    }

    @Test
    void rejectsInvalidQuantity() {
        assertThatThrownBy(() -> orderService.createOrder(customerId, request(item(pizzaId, 0))))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void cancelRestoresInventory() {
        OrderResponse order = orderService.createOrder(customerId, request(item(pizzaId, 2)));
        orderService.cancel(order.getId());

        assertThat(orderService.getById(order.getId()).getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(menuItemService.getById(pizzaId).getAvailableQuantity()).isEqualTo(20);
    }

    @Test
    void customerOrderHistoryIsReturned() {
        orderService.createOrder(customerId, request(item(pizzaId, 1)));
        assertThat(orderService.findByCustomer(customerId)).hasSize(1);
    }

    private MenuItemResponse createMenu(String name, String price, int qty) {
        MenuItemRequest request = new MenuItemRequest();
        request.setName(name);
        request.setDescription(name);
        request.setPrice(new BigDecimal(price));
        request.setAvailableQuantity(qty);
        request.setActive(true);
        return menuItemService.create(request);
    }

    private OrderRequest request(OrderItemRequest... items) {
        OrderRequest request = new OrderRequest();
        request.setItems(List.of(items));
        return request;
    }

    private OrderItemRequest item(Long menuItemId, int quantity) {
        OrderItemRequest request = new OrderItemRequest();
        request.setMenuItemId(menuItemId);
        request.setQuantity(quantity);
        return request;
    }
}
