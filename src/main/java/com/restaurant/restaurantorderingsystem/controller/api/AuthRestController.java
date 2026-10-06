package com.restaurant.restaurantorderingsystem.controller.api;

import com.restaurant.restaurantorderingsystem.dto.AuthResponse;
import com.restaurant.restaurantorderingsystem.dto.LoginRequest;
import com.restaurant.restaurantorderingsystem.dto.RegisterRequest;
import com.restaurant.restaurantorderingsystem.dto.UserResponse;
import com.restaurant.restaurantorderingsystem.security.JwtService;
import com.restaurant.restaurantorderingsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthRestController(
            UserService userService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.register(request);
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(jwtService.generateToken(details), user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );
        UserDetails details = userDetailsService.loadUserByUsername(request.getEmail());
        UserResponse user = userService.toResponse(userService.getByEmail(request.getEmail()));
        return ResponseEntity.ok(new AuthResponse(jwtService.generateToken(details), user));
    }
}
