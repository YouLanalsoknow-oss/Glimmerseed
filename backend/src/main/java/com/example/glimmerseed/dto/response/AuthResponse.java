package com.example.glimmerseed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private Long userId;
    
    public static AuthResponse success(String token, Long userId) {
        return new AuthResponse(true, "操作成功", token, userId);
    }
    
    public static AuthResponse error(String message) {
        return new AuthResponse(false, message, null, null);
    }
}