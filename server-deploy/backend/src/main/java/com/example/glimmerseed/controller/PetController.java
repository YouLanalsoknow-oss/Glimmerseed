package com.example.glimmerseed.controller;

import com.example.glimmerseed.dto.request.CreatePetRequest;
import com.example.glimmerseed.dto.response.ApiResponse;
import com.example.glimmerseed.dto.response.PetResponse;
import com.example.glimmerseed.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    private final PetService petService;
    
    public PetController(PetService petService) {
        this.petService = petService;
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<PetResponse>> createPet(
            @Valid @RequestBody CreatePetRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        PetResponse pet = petService.createPet(request, userId);
        return ResponseEntity.ok(ApiResponse.success(pet));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPets(
            @RequestHeader("X-User-Id") Long userId) {
        List<PetResponse> pets = petService.getPetsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(pets));
    }
    
    @GetMapping("/{petId}")
    public ResponseEntity<ApiResponse<PetResponse>> getPet(
            @PathVariable Long petId,
            @RequestHeader("X-User-Id") Long userId) {
        PetResponse pet = petService.getPetById(petId, userId);
        if (pet != null) {
            return ResponseEntity.ok(ApiResponse.success(pet));
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{petId}")
    public ResponseEntity<ApiResponse<Void>> deletePet(
            @PathVariable Long petId,
            @RequestHeader("X-User-Id") Long userId) {
        petService.deletePet(petId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}