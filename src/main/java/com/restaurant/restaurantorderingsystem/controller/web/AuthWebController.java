package com.restaurant.restaurantorderingsystem.controller.web;

import com.restaurant.restaurantorderingsystem.dto.RegisterRequest;
import com.restaurant.restaurantorderingsystem.exception.BusinessException;
import com.restaurant.restaurantorderingsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthWebController {

    private final UserService userService;

    public AuthWebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.register(request);
        } catch (BusinessException ex) {
            bindingResult.rejectValue("email", "exists", ex.getMessage());
            return "register";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Registration successful. Please sign in.");
        return "redirect:/login";
    }
}
