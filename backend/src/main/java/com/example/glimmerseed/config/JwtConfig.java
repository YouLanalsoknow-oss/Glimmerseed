package com.example.glimmerseed.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret}")
    private String secret;
    
    @Bean
    public SecretKey jwtSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}