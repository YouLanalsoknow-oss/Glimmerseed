package com.example.glimmerseed.config;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.util.List;
import java.util.Map;

public interface SiliconFlowApi {
    @POST("chat/completions")
    Call<SiliconFlowResponse> chat(
            @Header("Authorization") String auth,
            @Body Map<String, Object> body
    );
}

class SiliconFlowResponse {
    private List<Choice> choices;
    
    public List<Choice> getChoices() {
        return choices;
    }
    
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}

class Choice {
    private Message message;
    
    public Message getMessage() {
        return message;
    }
    
    public void setMessage(Message message) {
        this.message = message;
    }
}

class Message {
    private String role;
    private String content;
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}