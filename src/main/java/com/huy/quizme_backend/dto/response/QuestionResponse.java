package com.huy.quizme_backend.dto.response;

import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.QuestionOption;
import com.huy.quizme_backend.service.CloudinaryService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private String content;
    private String imageUrl;
    private Integer timeLimit;
    private Integer points;
    private Integer orderNumber;
    private String createdAt;
    private String updatedAt;
    private List<QuestionOptionResponse> options;
    
    // Chuyển đổi từ Question entity sang QuestionResponse DTO (không bao gồm options)
    public static QuestionResponse fromQuestion(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .quizId(question.getQuiz().getId())
                .quizTitle(question.getQuiz().getTitle())
                .content(question.getContent())
                .imageUrl(question.getImageUrl())
                .timeLimit(question.getTimeLimit())
                .points(question.getPoints())
                .orderNumber(question.getOrderNumber())
                .createdAt(question.getCreatedAt().toString())
                .updatedAt(question.getUpdatedAt() != null ? question.getUpdatedAt().toString() : null)
                .build();
    }
    
    // Chuyển đổi từ Question entity sang QuestionResponse DTO (không bao gồm options) với Cloudinary URL
    public static QuestionResponse fromQuestion(Question question, CloudinaryService cloudinaryService) {
        QuestionResponse response = fromQuestion(question);
        if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
            response.setImageUrl(cloudinaryService.getQuestionImageUrl(question.getImageUrl()));
        }
        return response;
    }
    
    // Chuyển đổi từ Question entity sang QuestionResponse DTO kèm options
    public static QuestionResponse fromQuestionWithOptions(Question question, List<QuestionOption> options) {
        QuestionResponse response = fromQuestion(question);
        if (options != null) {
            response.setOptions(options.stream()
                    .map(QuestionOptionResponse::fromQuestionOption)
                    .collect(Collectors.toList()));
        }
        return response;
    }
    
    // Chuyển đổi từ Question entity sang QuestionResponse DTO kèm options với Cloudinary URL
    public static QuestionResponse fromQuestionWithOptions(Question question, List<QuestionOption> options, CloudinaryService cloudinaryService) {
        QuestionResponse response = fromQuestion(question, cloudinaryService);
        if (options != null) {
            response.setOptions(options.stream()
                    .map(QuestionOptionResponse::fromQuestionOption)
                    .collect(Collectors.toList()));
        }
        return response;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionOptionResponse {
        private Long id;
        private String content;
        private Boolean isCorrect;
        
        // Chuyển đổi từ QuestionOption entity sang QuestionOptionResponse DTO
        public static QuestionOptionResponse fromQuestionOption(QuestionOption option) {
            return QuestionOptionResponse.builder()
                    .id(option.getId())
                    .content(option.getContent())
                    .isCorrect(option.getIsCorrect())
                    .build();
        }
    }
}