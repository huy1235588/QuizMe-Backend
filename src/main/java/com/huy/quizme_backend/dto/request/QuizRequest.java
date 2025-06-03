package com.huy.quizme_backend.dto.request;

import com.huy.quizme_backend.enity.enums.Difficulty;
import com.huy.quizme_backend.enity.enums.QuestionType;
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
public class QuizRequest {
    @NotBlank(message = "Quiz title cannot be blank")
    @Size(min = 2, max = 100, message = "Quiz title must be between 2 and 100 characters")
    private String title;

    private String description;

    private MultipartFile thumbnailFile;

    private List<Long> categoryIds;

    private Difficulty difficulty = Difficulty.MEDIUM;

    private Boolean isPublic = true;

    // Danh sách câu hỏi (optional - có thể tạo quiz mà không có câu hỏi)
    @Valid
    private List<QuizQuestionRequest> questions;

    /**
     * DTO cho câu hỏi trong quiz request
     * Khác với QuestionRequest ở chỗ không có quizId vì quiz chưa được tạo
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizQuestionRequest {
        @NotBlank(message = "Question content must not be empty")
        private String content;

        private MultipartFile imageFile;

        private MultipartFile audioFile;

        @Min(value = 5, message = "Time limit must be greater than or equal to 5 seconds")
        private Integer timeLimit = 30;

        @Min(value = 1, message = "Points must be greater than or equal to 1")
        private Integer points = 10;

        private Integer orderNumber;

        private QuestionType type = QuestionType.QUIZ;

        @NotNull(message = "There must be at least 2 options")
        @Size(min = 2, message = "There must be at least 2 options")
        @Valid
        private List<QuestionOptionRequest> options;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class QuestionOptionRequest {
            @NotBlank(message = "Option content must not be empty")
            private String content;

            @NotNull(message = "You must specify whether the option is correct or not")
            private Boolean isCorrect;
        }
    }
}