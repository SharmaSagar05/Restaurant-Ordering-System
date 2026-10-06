package com.restaurant.restaurantorderingsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.restaurantorderingsystem.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            response.sendRedirect("/login");
            return;
        }
        ApiErrorResponse body = new ApiErrorResponse();
        body.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        body.setError("Unauthorized");
        body.setMessage("Authentication is required");
        body.setPath(request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
