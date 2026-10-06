package com.restaurant.restaurantorderingsystem.controller.api;

import com.restaurant.restaurantorderingsystem.dto.AvailabilityRequest;
import com.restaurant.restaurantorderingsystem.dto.MenuItemRequest;
import com.restaurant.restaurantorderingsystem.dto.MenuItemResponse;
import com.restaurant.restaurantorderingsystem.service.MenuItemService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu")
public class MenuRestController {

    private final MenuItemService menuItemService;

    public MenuRestController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(menuItemService.search(q, activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getById(id));
    }

    @PostMapping
    public ResponseEntity<MenuItemResponse> create(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponse> update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.update(id, request));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<MenuItemResponse> availability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request
    ) {
        return ResponseEntity.ok(menuItemService.updateAvailability(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuItemService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
