package com.example.glimmerseed.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String appearance;
    
    @Column(nullable = false)
    private String personality;
    
    @Column(nullable = false)
    private String color;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_interacted_at")
    private LocalDateTime lastInteractedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastInteractedAt = LocalDateTime.now();
    }
}