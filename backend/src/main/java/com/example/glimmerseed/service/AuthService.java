package com.example.glimmerseed.service;

import com.example.glimmerseed.dto.request.LoginRequest;
import com.example.glimmerseed.dto.request.RegisterRequest;
import com.example.glimmerseed.dto.response.AuthResponse;
import com.example.glimmerseed.entity.User;
import com.example.glimmerseed.repository.UserRepository;
import com.example.glimmerseed.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                logger.warn("Registration failed - email already exists: {}", request.getEmail());
                return AuthResponse.builder()
                        .success(false)
                        .message("该邮箱已被注册")
                        .build();
            }

            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();

            userRepository.save(user);
            String token = jwtUtil.generateToken(request.getEmail());

            logger.info("User registered successfully: {}", request.getEmail());
            return AuthResponse.builder()
                    .success(true)
                    .message("注册成功")
                    .token(token)
                    .userId(user.getId())
                    .build();
        } catch (Exception e) {
            logger.error("Registration error for {}: {}", request.getEmail(), e.getMessage(), e);
            return AuthResponse.builder()
                    .success(false)
                    .message("注册失败: " + e.getMessage())
                    .build();
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            var userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                logger.warn("Login failed - user not found: {}", request.getEmail());
                return AuthResponse.builder()
                        .success(false)
                        .message("邮箱或密码错误")
                        .build();
            }

            User user = userOpt.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                logger.warn("Login failed - wrong password for: {}", request.getEmail());
                return AuthResponse.builder()
                        .success(false)
                        .message("邮箱或密码错误")
                        .build();
            }

            String token = jwtUtil.generateToken(request.getEmail());
            logger.info("User logged in successfully: {} (id={})", request.getEmail(), user.getId());
            return AuthResponse.builder()
                    .success(true)
                    .message("登录成功")
                    .token(token)
                    .userId(user.getId())
                    .build();
        } catch (Exception e) {
            logger.error("Login error for {}: {}", request.getEmail(), e.getMessage(), e);
            return AuthResponse.builder()
                    .success(false)
                    .message("登录失败: " + e.getMessage())
                    .build();
        }
    }
}
