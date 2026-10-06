package com.restaurant.restaurantorderingsystem.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.restaurant.restaurantorderingsystem.entity.MenuItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MenuItemRepositoryTest {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Test
    void searchActiveFindsMatchingItems() {
        menuItemRepository.save(item("Pizza", "Wood-fired", true));
        menuItemRepository.save(item("Hidden Pizza", "Secret", false));
        menuItemRepository.save(item("Burger", "Beef", true));

        List<MenuItem> results = menuItemRepository.searchActive("pizza");

        assertThat(results).extracting(MenuItem::getName).containsExactly("Pizza");
    }

    @Test
    void findByActiveTrueFiltersInactiveItems() {
        menuItemRepository.save(item("Salad", "Garden", true));
        menuItemRepository.save(item("Old Soup", "Retired", false));

        assertThat(menuItemRepository.findByActiveTrue()).hasSize(1);
    }

    private MenuItem item(String name, String description, boolean active) {
        MenuItem menuItem = new MenuItem();
        menuItem.setName(name);
        menuItem.setDescription(description);
        menuItem.setPrice(new BigDecimal("100.00"));
        menuItem.setAvailableQuantity(10);
        menuItem.setActive(active);
        return menuItem;
    }
}
