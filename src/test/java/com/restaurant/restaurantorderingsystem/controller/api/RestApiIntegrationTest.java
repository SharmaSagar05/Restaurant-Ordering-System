package com.restaurant.restaurantorderingsystem.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.restaurantorderingsystem.dto.LoginRequest;
import com.restaurant.restaurantorderingsystem.dto.MenuItemRequest;
import com.restaurant.restaurantorderingsystem.dto.OrderItemRequest;
import com.restaurant.restaurantorderingsystem.dto.OrderRequest;
import com.restaurant.restaurantorderingsystem.dto.RegisterRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginAndPlaceOrder() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setName("Api Customer");
        register.setEmail("api-customer@restaurant.com");
        register.setPassword("Customer1");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("api-customer@restaurant.com"))
                .andReturn();

        long customerId = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .path("user").path("id").asLong();

        LoginRequest login = new LoginRequest();
        login.setEmail("api-customer@restaurant.com");
        login.setPassword("Customer1");
        String customerToken = objectMapper.readTree(mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).path("token").asText();

        MenuItemRequest pizza = new MenuItemRequest();
        pizza.setName("Pizza");
        pizza.setDescription("Classic");
        pizza.setPrice(new BigDecimal("250.00"));
        pizza.setAvailableQuantity(20);
        pizza.setActive(true);

        long pizzaId = objectMapper.readTree(mockMvc.perform(post("/api/menu")
                        .with(user("admin@restaurant.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pizza)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).path("id").asLong();

        MenuItemRequest burger = new MenuItemRequest();
        burger.setName("Burger");
        burger.setDescription("Grilled");
        burger.setPrice(new BigDecimal("150.00"));
        burger.setAvailableQuantity(10);
        burger.setActive(true);
        long burgerId = objectMapper.readTree(mockMvc.perform(post("/api/menu")
                        .with(user("admin@restaurant.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(burger)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk());

        OrderRequest order = new OrderRequest();
        order.setItems(List.of(line(pizzaId, 2), line(burgerId, 1)));

        mockMvc.perform(post("/api/orders/customer/" + customerId)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(650.0));

        mockMvc.perform(post("/api/orders/customer/" + customerId)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(line(burgerId, 99)))))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelEndpointRestoresEligibilityRules() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setName("Cancel Customer");
        register.setEmail("cancel-customer@restaurant.com");
        register.setPassword("Customer1");
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();
        String body = registerResult.getResponse().getContentAsString();
        long customerId = objectMapper.readTree(body).path("user").path("id").asLong();
        String token = objectMapper.readTree(body).path("token").asText();

        MenuItemRequest tea = new MenuItemRequest();
        tea.setName("Iced Tea");
        tea.setPrice(new BigDecimal("40.00"));
        tea.setAvailableQuantity(5);
        tea.setActive(true);
        long teaId = objectMapper.readTree(mockMvc.perform(post("/api/menu")
                        .with(user("admin@restaurant.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tea)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).path("id").asLong();

        long orderId = objectMapper.readTree(mockMvc.perform(post("/api/orders/customer/" + customerId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(line(teaId, 1)))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(patch("/api/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private OrderRequest request(OrderItemRequest... items) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(items));
        return orderRequest;
    }

    private OrderItemRequest line(Long id, int qty) {
        OrderItemRequest item = new OrderItemRequest();
        item.setMenuItemId(id);
        item.setQuantity(qty);
        return item;
    }
}
