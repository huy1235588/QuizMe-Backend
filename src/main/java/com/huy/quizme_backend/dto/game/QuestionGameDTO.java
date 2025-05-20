package com.huy.quizme_backend.dto.game;

import com.huy.quizme_backend.enity.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO dùng để gửi câu hỏi tới client trong quá trình chơi game
 * Không chứa thông tin về đáp án đúng để đảm bảo công bằng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGameDTO {
    private Long questionId;
    private String content;
    private String imageUrl;
    private String audioUrl;
    private String videoUrl;
    private QuestionType type;
    private Integer timeLimit;
    private Integer points;
    private Integer questionNumber;
    private Integer totalQuestions;
    private List<QuestionOptionDTO> options;

    /**
     * DTO lồng cho các lựa chọn
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOptionDTO {
        private Long id;
        private String content;
    }
}
