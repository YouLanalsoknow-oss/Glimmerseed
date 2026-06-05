package com.example.glimmerseed.service;

import com.example.glimmerseed.dto.request.CreatePetRequest;
import com.example.glimmerseed.dto.response.PetResponse;
import com.example.glimmerseed.entity.Pet;
import com.example.glimmerseed.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetService {
    private final PetRepository petRepository;
    
    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }
    
    public PetResponse createPet(CreatePetRequest request, Long userId) {
        Pet pet = new Pet();
        pet.setName(request.getName());
        pet.setAppearance(request.getAppearance());
        pet.setPersonality(request.getPersonality());
        pet.setColor(request.getColor());
        pet.setUserId(userId);
        
        Pet savedPet = petRepository.save(pet);
        return PetResponse.fromEntity(savedPet);
    }
    
    public List<PetResponse> getPetsByUser(Long userId) {
        return petRepository.findByUserIdOrderByLastInteractedAtDesc(userId)
                .stream()
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public PetResponse getPetById(Long petId, Long userId) {
        return petRepository.findById(petId)
                .filter(pet -> pet.getUserId().equals(userId))
                .map(PetResponse::fromEntity)
                .orElse(null);
    }
    
    public void updateLastInteracted(Long petId) {
        petRepository.updateLastInteractedAt(petId, LocalDateTime.now());
    }
    
    public void deletePet(Long petId, Long userId) {
        petRepository.findById(petId)
                .filter(pet -> pet.getUserId().equals(userId))
                .ifPresent(petRepository::delete);
    }
}