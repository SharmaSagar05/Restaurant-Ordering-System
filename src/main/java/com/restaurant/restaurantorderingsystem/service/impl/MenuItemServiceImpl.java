package com.restaurant.restaurantorderingsystem.service.impl;

import com.restaurant.restaurantorderingsystem.dto.AvailabilityRequest;
import com.restaurant.restaurantorderingsystem.dto.MenuItemRequest;
import com.restaurant.restaurantorderingsystem.dto.MenuItemResponse;
import com.restaurant.restaurantorderingsystem.entity.MenuItem;
import com.restaurant.restaurantorderingsystem.exception.ResourceNotFoundException;
import com.restaurant.restaurantorderingsystem.repository.MenuItemRepository;
import com.restaurant.restaurantorderingsystem.service.MenuItemService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public MenuItemServiceImpl(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    @Transactional
    public MenuItemResponse create(MenuItemRequest request) {
        MenuItem item = new MenuItem();
        apply(item, request);
        return Mapper.toMenuItemResponse(menuItemRepository.save(item));
    }

    @Override
    @Transactional
    public MenuItemResponse update(Long id, MenuItemRequest request) {
        MenuItem item = getEntity(id);
        apply(item, request);
        return Mapper.toMenuItemResponse(menuItemRepository.save(item));
    }

    @Override
    @Transactional
    public MenuItemResponse updateAvailability(Long id, AvailabilityRequest request) {
        MenuItem item = getEntity(id);
        item.setAvailableQuantity(request.getAvailableQuantity());
        if (request.getActive() != null) {
            item.setActive(request.getActive());
        }
        return Mapper.toMenuItemResponse(menuItemRepository.save(item));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        MenuItem item = getEntity(id);
        item.setActive(false);
        menuItemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getById(Long id) {
        return Mapper.toMenuItemResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> findAll() {
        return menuItemRepository.findAll().stream().map(Mapper::toMenuItemResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> findActive() {
        return menuItemRepository.findByActiveTrue().stream().map(Mapper::toMenuItemResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> search(String keyword, boolean activeOnly) {
        if (!StringUtils.hasText(keyword)) {
            return activeOnly ? findActive() : findAll();
        }
        List<MenuItem> items = activeOnly
                ? menuItemRepository.searchActive(keyword.trim())
                : menuItemRepository.searchAll(keyword.trim());
        return items.stream().map(Mapper::toMenuItemResponse).toList();
    }

    private MenuItem getEntity(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
    }

    private void apply(MenuItem item, MenuItemRequest request) {
        item.setName(request.getName().trim());
        item.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        item.setPrice(request.getPrice());
        item.setAvailableQuantity(request.getAvailableQuantity());
        item.setActive(request.getActive() == null || request.getActive());
    }
}
