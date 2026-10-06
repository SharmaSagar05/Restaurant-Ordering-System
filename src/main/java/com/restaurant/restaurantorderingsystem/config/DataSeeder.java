package com.restaurant.restaurantorderingsystem.config;

import com.restaurant.restaurantorderingsystem.entity.MenuItem;
import com.restaurant.restaurantorderingsystem.entity.Role;
import com.restaurant.restaurantorderingsystem.entity.User;
import com.restaurant.restaurantorderingsystem.repository.MenuItemRepository;
import com.restaurant.restaurantorderingsystem.repository.UserRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            MenuItemRepository menuItemRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.menuItemRepository = menuItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser("Admin User", "admin@restaurant.com", "Admin@123", Role.ADMIN);
        seedUser("Staff Member", "staff@restaurant.com", "Staff@123", Role.STAFF);
        seedUser("Demo Customer", "customer@restaurant.com", "Customer@123", Role.CUSTOMER);

        if (menuItemRepository.count() == 0) {
            menuItemRepository.save(item("Pizza", "Wood-fired pizza with mozzarella and basil.", "250.00", 20));
            menuItemRepository.save(item("Burger", "Grilled beef burger with lettuce and tomato.", "150.00", 25));
            menuItemRepository.save(item("Pasta", "Creamy Alfredo pasta with herbs.", "180.00", 15));
            menuItemRepository.save(item("Salad", "Fresh garden salad with house dressing.", "90.00", 30));
            menuItemRepository.save(item("Iced Tea", "Chilled lemon iced tea.", "40.00", 50));
        }
    }

    private void seedUser(String name, String email, String password, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        userRepository.save(user);
    }

    private MenuItem item(String name, String description, String price, int quantity) {
        MenuItem menuItem = new MenuItem();
        menuItem.setName(name);
        menuItem.setDescription(description);
        menuItem.setPrice(new BigDecimal(price));
        menuItem.setAvailableQuantity(quantity);
        menuItem.setActive(true);
        return menuItem;
    }
}
