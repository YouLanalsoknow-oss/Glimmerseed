package com.example.glimmerseed.controller;

import com.example.glimmerseed.dto.request.ChatRequest;
import com.example.glimmerseed.dto.response.ApiResponse;
import com.example.glimmerseed.dto.response.ChatMessageResponse;
import com.example.glimmerseed.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;
    
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        ChatMessageResponse message = chatService.sendMessage(request.getPetId(), request.getContent(), userId);
        if (message != null) {
            return ResponseEntity.ok(ApiResponse.success(message));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("发送失败"));
    }
    
    @GetMapping("/{petId}")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable Long petId,
            @RequestHeader("X-User-Id") Long userId) {
        List<ChatMessageResponse> messages = chatService.getMessages(petId, userId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
}