package com.example.glimmerseed.repository;

import com.example.glimmerseed.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByUserIdOrderByLastInteractedAtDesc(Long userId);
    
    @Modifying
    @Query("UPDATE Pet p SET p.lastInteractedAt = :time WHERE p.id = :id")
    void updateLastInteractedAt(Long id, LocalDateTime time);
}