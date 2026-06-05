package com.example.glimmerseed.dto.response;

import com.example.glimmerseed.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long petId;
    private String content;
    private String role;
    private LocalDateTime timestamp;
    
    public static ChatMessageResponse fromEntity(ChatMessage message) {
        return new ChatMessageResponse(
            message.getId(),
            message.getPetId(),
            message.getContent(),
            message.getRole(),
            message.getTimestamp()
        );
    }
}