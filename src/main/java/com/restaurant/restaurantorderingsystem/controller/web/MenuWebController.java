package com.restaurant.restaurantorderingsystem.controller.web;

import com.restaurant.restaurantorderingsystem.dto.MenuItemRequest;
import com.restaurant.restaurantorderingsystem.dto.MenuItemResponse;
import com.restaurant.restaurantorderingsystem.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MenuWebController {

    private final MenuItemService menuItemService;

    public MenuWebController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @GetMapping({"/", "/menu"})
    public String menu(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("menuItems", menuItemService.search(q, true));
        model.addAttribute("q", q);
        return "menu";
    }

    @GetMapping("/menu/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addForm(Model model) {
        if (!model.containsAttribute("menuItemRequest")) {
            model.addAttribute("menuItemRequest", new MenuItemRequest());
        }
        model.addAttribute("formTitle", "Add Menu Item");
        model.addAttribute("formAction", "/menu/add");
        return "menu-form";
    }

    @PostMapping("/menu/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String add(
            @Valid @ModelAttribute("menuItemRequest") MenuItemRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Add Menu Item");
            model.addAttribute("formAction", "/menu/add");
            return "menu-form";
        }
        menuItemService.create(request);
        redirectAttributes.addFlashAttribute("successMessage", "Menu item added.");
        return "redirect:/admin";
    }

    @GetMapping("/menu/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        MenuItemResponse item = menuItemService.getById(id);
        MenuItemRequest request = new MenuItemRequest();
        request.setName(item.getName());
        request.setDescription(item.getDescription());
        request.setPrice(item.getPrice());
        request.setAvailableQuantity(item.getAvailableQuantity());
        request.setActive(item.isActive());
        model.addAttribute("menuItemRequest", request);
        model.addAttribute("formTitle", "Edit Menu Item");
        model.addAttribute("formAction", "/menu/edit/" + id);
        return "menu-form";
    }

    @PostMapping("/menu/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("menuItemRequest") MenuItemRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Edit Menu Item");
            model.addAttribute("formAction", "/menu/edit/" + id);
            return "menu-form";
        }
        menuItemService.update(id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Menu item updated.");
        return "redirect:/admin";
    }

    @PostMapping("/menu/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        menuItemService.deactivate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Menu item deactivated.");
        return "redirect:/admin";
    }
}
