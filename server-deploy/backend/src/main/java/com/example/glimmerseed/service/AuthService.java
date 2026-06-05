package com.example.glimmerseed.service;

import com.example.glimmerseed.dto.request.LoginRequest;
import com.example.glimmerseed.dto.request.RegisterRequest;
import com.example.glimmerseed.dto.response.AuthResponse;
import com.example.glimmerseed.entity.User;
import com.example.glimmerseed.repository.UserRepository;
import com.example.glimmerseed.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.error("邮箱已被注册");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        
        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail());
        
        return AuthResponse.success(token, savedUser.getId());
    }
    
    public AuthResponse login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        String token = jwtUtil.generateToken(user.getEmail());
                        return AuthResponse.success(token, user.getId());
                    }
                    return AuthResponse.error("密码错误");
                })
                .orElse(AuthResponse.error("用户不存在"));
    }
}