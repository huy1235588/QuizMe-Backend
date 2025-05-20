package com.huy.quizme_backend.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO dùng để hiển thị kết quả cuối cùng sau khi kết thúc trò chơi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResultDTO {
    private Long roomId;
    private String quizTitle;
    private Integer totalQuestions;
    private Integer duration;
    private List<FinalRankingDTO> finalRankings;
    private List<QuestionStatDTO> questionStats;

    /**
     * DTO cho thông tin xếp hạng của một người chơi
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinalRankingDTO {
        private Long userId;
        private String username;
        private Integer score;
        private Integer rank;
        private String avatar;
        private Integer correctAnswers;
        private Boolean isGuest;
    }

    /**
     * DTO cho thống kê của từng câu hỏi
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionStatDTO {
        private Long questionId;
        private Integer correctPercentage;
    }
}
