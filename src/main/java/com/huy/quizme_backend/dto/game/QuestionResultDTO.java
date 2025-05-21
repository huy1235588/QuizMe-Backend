package com.huy.quizme_backend.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO dùng để gửi kết quả câu hỏi về client sau khi mọi người đã trả lời
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResultDTO {
    private Long questionId;
    private List<Long> correctOptions;
    private String explanation;
    private String funFact;
    private List<OptionStatDTO> optionStats;
    private UserAnswerDTO userAnswer;

    /**
     * DTO cho thống kê tỷ lệ lựa chọn
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionStatDTO {
        private Long optionId;
        private Double percentage;
    }

    /**
     * DTO cho thông tin câu trả lời của người dùng
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAnswerDTO {
        private Boolean isCorrect;
        private Integer score;
        private Double timeTaken;
    }
}
