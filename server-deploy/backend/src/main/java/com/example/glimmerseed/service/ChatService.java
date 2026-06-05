package com.example.glimmerseed.service;

import com.example.glimmerseed.dto.response.ChatMessageResponse;
import com.example.glimmerseed.entity.ChatMessage;
import com.example.glimmerseed.entity.Pet;
import com.example.glimmerseed.repository.ChatMessageRepository;
import com.example.glimmerseed.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final PetRepository petRepository;
    private final SiliconFlowService siliconFlowService;
    
    public ChatService(ChatMessageRepository chatMessageRepository, 
                      PetRepository petRepository,
                      SiliconFlowService siliconFlowService) {
        this.chatMessageRepository = chatMessageRepository;
        this.petRepository = petRepository;
        this.siliconFlowService = siliconFlowService;
    }
    
    public ChatMessageResponse sendMessage(Long petId, String content, Long userId) {
        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null || !pet.getUserId().equals(userId)) {
            return null;
        }
        
        ChatMessage userMessage = new ChatMessage();
        userMessage.setPetId(petId);
        userMessage.setContent(content);
        userMessage.setRole("user");
        chatMessageRepository.save(userMessage);
        
        List<ChatMessage> history = chatMessageRepository.findByPetIdOrderByTimestampAsc(petId);
        List<Map<String, String>> historyList = history.stream()
                .map(msg -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("role", msg.getRole());
                    map.put("content", msg.getContent());
                    return map;
                })
                .collect(Collectors.toList());
        
        String responseContent = siliconFlowService.chat(content, historyList);
        
        ChatMessage botMessage = new ChatMessage();
        botMessage.setPetId(petId);
        botMessage.setContent(responseContent);
        botMessage.setRole("assistant");
        ChatMessage savedMessage = chatMessageRepository.save(botMessage);
        
        petRepository.updateLastInteractedAt(petId, java.time.LocalDateTime.now());
        
        return ChatMessageResponse.fromEntity(savedMessage);
    }
    
    public List<ChatMessageResponse> getMessages(Long petId, Long userId) {
        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null || !pet.getUserId().equals(userId)) {
            return new ArrayList<>();
        }
        
        return chatMessageRepository.findByPetIdOrderByTimestampAsc(petId)
                .stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }
}