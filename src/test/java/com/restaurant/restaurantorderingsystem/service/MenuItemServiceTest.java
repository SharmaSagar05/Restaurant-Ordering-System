package com.restaurant.restaurantorderingsystem.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.restaurantorderingsystem.dto.AvailabilityRequest;
import com.restaurant.restaurantorderingsystem.dto.MenuItemRequest;
import com.restaurant.restaurantorderingsystem.dto.MenuItemResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MenuItemServiceTest {

    @Autowired
    private MenuItemService menuItemService;

    @Test
    void crudAndAvailabilityWork() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Pasta");
        request.setDescription("Creamy");
        request.setPrice(new BigDecimal("180.00"));
        request.setAvailableQuantity(15);
        request.setActive(true);

        MenuItemResponse created = menuItemService.create(request);
        assertThat(created.getId()).isNotNull();
        assertThat(menuItemService.getById(created.getId()).getName()).isEqualTo("Pasta");

        request.setPrice(new BigDecimal("190.00"));
        MenuItemResponse updated = menuItemService.update(created.getId(), request);
        assertThat(updated.getPrice()).isEqualByComparingTo("190.00");

        AvailabilityRequest availability = new AvailabilityRequest();
        availability.setAvailableQuantity(8);
        availability.setActive(true);
        assertThat(menuItemService.updateAvailability(created.getId(), availability).getAvailableQuantity()).isEqualTo(8);

        menuItemService.deactivate(created.getId());
        assertThat(menuItemService.getById(created.getId()).isActive()).isFalse();
        assertThat(menuItemService.findActive()).extracting(MenuItemResponse::getId).doesNotContain(created.getId());
    }
}
