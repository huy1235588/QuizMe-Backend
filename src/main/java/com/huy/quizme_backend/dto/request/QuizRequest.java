package com.huy.quizme_backend.dto.request;

import com.huy.quizme_backend.enity.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizRequest {
    @NotBlank(message = "Quiz title cannot be blank")
    @Size(min = 2, max = 100, message = "Quiz title must be between 2 and 100 characters")
    private String title;
    
    private String description;
    
    private MultipartFile thumbnailFile;
    
    private List<Long> categoryIds;
    
    private Difficulty difficulty = Difficulty.MEDIUM;
    
    private Boolean isPublic = true;
}