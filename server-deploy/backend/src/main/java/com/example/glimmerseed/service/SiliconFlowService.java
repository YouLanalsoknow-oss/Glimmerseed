package com.example.glimmerseed.service;

import com.example.glimmerseed.config.SiliconFlowApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SiliconFlowService {
    private final SiliconFlowApi siliconFlowApi;
    
    @Value("${siliconflow.api-key}")
    private String apiKey;
    
    public SiliconFlowService(SiliconFlowApi siliconFlowApi) {
        this.siliconFlowApi = siliconFlowApi;
    }
    
    public String chat(String content, List<Map<String, String>> history) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "deepseek-ai/DeepSeek-V3");
            body.put("temperature", 0.7);
            body.put("max_tokens", 1024);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个可爱的AI桌宠，性格活泼可爱，用简短友好的语言回答问题。");
            messages.add(systemMsg);
            
            for (Map<String, String> msg : history) {
                messages.add(msg);
            }
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", content);
            messages.add(userMsg);
            
            body.put("messages", messages);
            
            Call<com.example.glimmerseed.config.SiliconFlowResponse> call = siliconFlowApi.chat(
                    "Bearer " + apiKey,
                    body
            );
            
            Response<com.example.glimmerseed.config.SiliconFlowResponse> response = call.execute();
            
            if (response.isSuccessful() && response.body() != null) {
                List<com.example.glimmerseed.config.Choice> choices = response.body().getChoices();
                if (choices != null && !choices.isEmpty()) {
                    com.example.glimmerseed.config.Message message = choices.get(0).getMessage();
                    if (message != null) {
                        return message.getContent();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "抱歉，我暂时无法回答你的问题。";
    }
}