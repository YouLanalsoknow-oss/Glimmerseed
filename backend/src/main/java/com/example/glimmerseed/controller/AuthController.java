package com.example.glimmerseed.controller;

import com.example.glimmerseed.dto.request.LoginRequest;
import com.example.glimmerseed.dto.request.RegisterRequest;
import com.example.glimmerseed.dto.response.AuthResponse;
import com.example.glimmerseed.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        logger.info("Register request for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        logger.info("Login request for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }
}
