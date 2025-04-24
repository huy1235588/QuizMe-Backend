package com.huy.quizme_backend.dto.response;

import com.huy.quizme_backend.enity.Difficulty;
import com.huy.quizme_backend.enity.Quiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private String quizThumbnails;
    private Long categoryId;
    private String categoryName;
    private Long creatorId;
    private String creatorName;
    private Difficulty difficulty;
    private Boolean isPublic;
    private Integer playCount;
    private Integer questionCount;
    private String createdAt;
    private String updatedAt;

    /// Chuyển đổi từ Quiz entity sang QuizResponse DTO
    public static QuizResponse fromQuiz(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .quizThumbnails(quiz.getQuizThumbnails())
                .categoryId(quiz.getCategory() != null ? quiz.getCategory().getId() : null)
                .categoryName(quiz.getCategory() != null ? quiz.getCategory().getName() : null)
                .creatorId(quiz.getCreator().getId())
                .creatorName(quiz.getCreator().getFullName())
                .difficulty(quiz.getDifficulty())
                .isPublic(quiz.getIsPublic())
                .playCount(quiz.getPlayCount())
                .questionCount(quiz.getQuestionCount())
                .createdAt(quiz.getCreatedAt().toString())
                .updatedAt(quiz.getUpdatedAt() != null ? quiz.getUpdatedAt().toString() : null)
                .build();
    }
}