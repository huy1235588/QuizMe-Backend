package com.huy.quizme_backend.dto.response;

import com.huy.quizme_backend.enity.Category;
import com.huy.quizme_backend.enity.enums.Difficulty;
import com.huy.quizme_backend.enity.Quiz;
import com.huy.quizme_backend.service.LocalStorageService;
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
public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private String quizThumbnails;
    private List<Long> categoryIds;
    private List<String> categoryNames;
    private Long creatorId;
    private String creatorName;
    private String creatorAvatar;
    private Difficulty difficulty;
    private Boolean isPublic;
    private Integer playCount;
    private Integer questionCount;
    private Integer favoriteCount;
    private String createdAt;
    private String updatedAt;

    // Chuyển đổi từ Quiz entity sang QuizResponse DTO
    public static QuizResponse fromQuiz(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .quizThumbnails(quiz.getQuizThumbnails())
                .categoryIds(quiz.getCategories() != null ?
                        quiz.getCategories().stream().map(Category::getId).collect(Collectors.toList()) :
                        null)
                .categoryNames(quiz.getCategories() != null ?
                        quiz.getCategories().stream().map(Category::getName).collect(Collectors.toList()) :
                        null)
                .creatorAvatar(quiz.getCreator().getProfileImage())
                .creatorId(quiz.getCreator().getId())
                .creatorName(quiz.getCreator().getFullName())
                .difficulty(quiz.getDifficulty())
                .isPublic(quiz.getIsPublic())
                .playCount(quiz.getPlayCount())
                .questionCount(quiz.getQuestionCount())
                .favoriteCount(quiz.getFavoriteCount())
                .createdAt(quiz.getCreatedAt().toString())
                .updatedAt(quiz.getUpdatedAt() != null ? quiz.getUpdatedAt().toString() : null)
                .build();
    }

    // Chuyển đổi từ Quiz entity sang QuizResponse DTO với Cloudinary URL
    public static QuizResponse fromQuiz(Quiz quiz, LocalStorageService localStorageService) {
        QuizResponse response = fromQuiz(quiz);
        if (quiz.getQuizThumbnails() != null && !quiz.getQuizThumbnails().isEmpty()) {
            response.setQuizThumbnails(localStorageService.getQuizThumbnailUrl(quiz.getQuizThumbnails()));
        }

        // Nếu có ảnh đại diện của người tạo quiz
        if (quiz.getCreator().getProfileImage() != null && !quiz.getCreator().getProfileImage().isEmpty()) {
            response.setCreatorAvatar(localStorageService.getProfileImageUrl(quiz.getCreator().getProfileImage()));
        }

        return response;
    }
}