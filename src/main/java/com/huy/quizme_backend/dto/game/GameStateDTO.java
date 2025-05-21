package com.huy.quizme_backend.dto.game;

import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.Room;
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

    // Chuyển đổi từ entity sang DTO
    public static GameStateDTO fromEntity(
            Room room,
            Question currentQuestion,
            QuestionGameDTO questionDTO,
            Integer remainingTime,
            LeaderboardDTO leaderboard,
            Integer questionNumber,
            Integer totalQuestions
    ) {
        return GameStateDTO.builder()
                .gameActive(room.getStatus().name().equals("IN_PROGRESS"))
                .currentQuestion(questionDTO)
                .remainingTime(remainingTime)
                .leaderboard(leaderboard)
                .questionNumber(questionNumber)
                .totalQuestions(totalQuestions)
                .build();
    }

    // Tạo GameStateDTO với trạng thái không hoạt động
    public static GameStateDTO inactive() {
        return GameStateDTO.builder()
                .gameActive(false)
                .build();
    }
}
