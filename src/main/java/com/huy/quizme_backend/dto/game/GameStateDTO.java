package com.huy.quizme_backend.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dùng để truyền trạng thái hiện tại của trò chơi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStateDTO {
    private boolean gameActive;
    private QuestionGameDTO currentQuestion;
    private Integer remainingTime;
    private LeaderboardDTO leaderboard;
    private Integer questionNumber;
    private Integer totalQuestions;
}
