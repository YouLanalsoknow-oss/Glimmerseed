package com.example.glimmerseed.dto.response;

import com.example.glimmerseed.entity.Pet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetResponse {
    private Long id;
    private String name;
    private String appearance;
    private String personality;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime lastInteractedAt;
    
    public static PetResponse fromEntity(Pet pet) {
        return new PetResponse(
            pet.getId(),
            pet.getName(),
            pet.getAppearance(),
            pet.getPersonality(),
            pet.getColor(),
            pet.getCreatedAt(),
            pet.getLastInteractedAt()
        );
    }
}