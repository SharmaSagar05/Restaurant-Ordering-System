package com.restaurant.restaurantorderingsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.restaurantorderingsystem.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            response.sendRedirect("/login?error=access");
            return;
        }
        ApiErrorResponse body = new ApiErrorResponse();
        body.setStatus(HttpServletResponse.SC_FORBIDDEN);
        body.setError("Forbidden");
        body.setMessage("You do not have permission to perform this action");
        body.setPath(request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
