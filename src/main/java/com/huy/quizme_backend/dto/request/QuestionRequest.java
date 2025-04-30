package com.huy.quizme_backend.dto.request;

import com.huy.quizme_backend.enity.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
public class QuestionRequest {
    @NotNull(message = "Quiz ID không được để trống")
    private Long quizId;
    
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String content;
    
    private MultipartFile imageFile;

    private MultipartFile audioFile;
    
    @Min(value = 5, message = "Thời gian phải lớn hơn hoặc bằng 5 giây")
    private Integer timeLimit = 30;
    
    @Min(value = 1, message = "Điểm phải lớn hơn hoặc bằng 1")
    private Integer points = 10;
    
    private Integer orderNumber;

    private QuestionType type;
    
    @NotNull(message = "Phải có ít nhất 2 lựa chọn")
    @Size(min = 2, message = "Phải có ít nhất 2 lựa chọn")
    @Valid
    private List<QuestionOptionRequest> options;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionOptionRequest {
        @NotBlank(message = "Nội dung lựa chọn không được để trống")
        private String content;
        
        @NotNull(message = "Phải xác định lựa chọn đúng hay sai")
        private Boolean isCorrect;
    }
}