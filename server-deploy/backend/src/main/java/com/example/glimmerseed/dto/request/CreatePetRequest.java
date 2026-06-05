package com.example.glimmerseed.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePetRequest {
    @NotBlank(message = "名称不能为空")
    private String name;
    
    @NotBlank(message = "外观不能为空")
    private String appearance;
    
    @NotBlank(message = "性格不能为空")
    private String personality;
    
    @NotBlank(message = "颜色不能为空")
    private String color;
}