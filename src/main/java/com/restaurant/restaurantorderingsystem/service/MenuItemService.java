package com.restaurant.restaurantorderingsystem.service;

import com.restaurant.restaurantorderingsystem.dto.AvailabilityRequest;
import com.restaurant.restaurantorderingsystem.dto.MenuItemRequest;
import com.restaurant.restaurantorderingsystem.dto.MenuItemResponse;
import java.util.List;

public interface MenuItemService {

    MenuItemResponse create(MenuItemRequest request);

    MenuItemResponse update(Long id, MenuItemRequest request);

    MenuItemResponse updateAvailability(Long id, AvailabilityRequest request);

    void deactivate(Long id);

    MenuItemResponse getById(Long id);

    List<MenuItemResponse> findAll();

    List<MenuItemResponse> findActive();

    List<MenuItemResponse> search(String keyword, boolean activeOnly);
}
