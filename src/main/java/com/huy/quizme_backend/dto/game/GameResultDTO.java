package com.huy.quizme_backend.dto.game;

import com.huy.quizme_backend.enity.GameResult;
import com.huy.quizme_backend.enity.GameResultQuestion;
import com.huy.quizme_backend.enity.RoomParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<FinalPlayerRankingDTO> finalRankings;
    private List<QuestionStatDTO> questionStats;

    /**
     * DTO cho thông tin xếp hạng cuối cùng của một người chơi
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinalPlayerRankingDTO {
        private Long userId;
        private String username;
        private Integer score;
        private Integer rank;
        private String avatar;
        private Integer correctAnswers;
        private Boolean isGuest;

        // Chuyển đổi từ entity sang DTO
        public static FinalPlayerRankingDTO fromEntity(RoomParticipant participant, Integer rank, Integer correctAnswers) {
            return FinalPlayerRankingDTO.builder()
                    .userId(participant.getUser() != null ? participant.getUser().getId() : null)
                    .username(participant.getUser() != null ? participant.getUser().getUsername() : participant.getGuestName())
                    .score(participant.getScore())
                    .rank(rank)
                    .avatar(participant.getUser() != null ? participant.getUser().getProfileImage() : null)
                    .correctAnswers(correctAnswers)
                    .isGuest(participant.isGuest())
                    .build();
        }
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
        private Double correctPercentage;

        // Chuyển đổi từ entity sang DTO
        public static QuestionStatDTO fromEntity(GameResultQuestion resultQuestion) {
            int total = resultQuestion.getCorrectCount() + resultQuestion.getIncorrectCount();
            double percentage = total > 0 ?
                    (double) resultQuestion.getCorrectCount() / total * 100 : 0;

            return QuestionStatDTO.builder()
                    .questionId(resultQuestion.getQuestion().getId())
                    .correctPercentage(percentage)
                    .build();
        }
    }

    // Chuyển đổi từ entity sang DTO
    public static GameResultDTO fromEntity(
            GameResult gameResult,
            List<RoomParticipant> participants,
            List<Integer> correctAnswers
    ) {
        // Sắp xếp người chơi theo điểm số giảm dần
        List<RoomParticipant> sortedParticipants = participants.stream()
                .sorted(Comparator.comparing(RoomParticipant::getScore).reversed())
                .toList();

        // Tạo các DTO xếp hạng
        List<FinalPlayerRankingDTO> rankings = sortedParticipants.stream()
                .map(participant -> {
                    int index = participants.indexOf(participant);
                    int rank = sortedParticipants.indexOf(participant) + 1;
                    Integer correctCount = (index < correctAnswers.size()) ? correctAnswers.get(index) : 0;
                    return FinalPlayerRankingDTO.fromEntity(participant, rank, correctCount);
                })
                .collect(Collectors.toList());

        // Tạo các thống kê câu hỏi
        List<QuestionStatDTO> questionStats = gameResult.getGameResultQuestions().stream()
                .map(QuestionStatDTO::fromEntity)
                .collect(Collectors.toList());

        // Tính thời gian chơi game (giây)
        Integer duration = null;
        if (gameResult.getStartTime() != null && gameResult.getEndTime() != null) {
            duration = (int) java.time.Duration.between(
                    gameResult.getStartTime(), gameResult.getEndTime()).getSeconds();
        }

        return GameResultDTO.builder()
                .roomId(gameResult.getRoom().getId())
                .quizTitle(gameResult.getQuiz().getTitle())
                .totalQuestions(gameResult.getQuestionCount())
                .duration(duration)
                .finalRankings(rankings)
                .questionStats(questionStats)
                .build();
    }
}
