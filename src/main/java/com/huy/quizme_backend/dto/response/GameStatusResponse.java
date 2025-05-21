package com.huy.quizme_backend.dto.response;

import com.huy.quizme_backend.dto.game.GameStateDTO;
import com.huy.quizme_backend.dto.game.QuestionGameDTO;
import com.huy.quizme_backend.dto.game.LeaderboardDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho trạng thái trò chơi hiện tại
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStatusResponse {
    private boolean gameActive;
    private QuestionGameDTO currentQuestion;
    private Integer remainingTime;
    private LeaderboardDTO leaderboard;
    private Integer questionNumber;
    private Integer totalQuestions;

    public static GameStatusResponse fromGameState(GameStateDTO state) {
        return GameStatusResponse.builder()
                .gameActive(state.isGameActive())
                .currentQuestion(state.getCurrentQuestion())
                .remainingTime(state.getRemainingTime())
                .leaderboard(state.getLeaderboard())
                .questionNumber(state.getQuestionNumber())
                .totalQuestions(state.getTotalQuestions())
                .build();
    }
}
