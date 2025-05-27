package com.huy.quizme_backend.dto.game;

import com.huy.quizme_backend.enity.GamePlayerAnswer;
import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.QuestionOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

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
    private List<UserAnswerDTO> userAnswer;

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

        // Chuyển đổi từ entity sang DTO
        public static OptionStatDTO fromEntity(QuestionOption option, Double percentage) {
            return OptionStatDTO.builder()
                    .optionId(option.getId())
                    .percentage(percentage)
                    .build();
        }
    }

    /**
     * DTO cho thông tin câu trả lời của người dùng
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAnswerDTO {
        private  Long userId;
        private Boolean isCorrect;
        private Integer score;
        private Double timeTaken;

        // Chuyển đổi từ entity sang DTO
        public static UserAnswerDTO fromEntity(GamePlayerAnswer playerAnswer) {
            return UserAnswerDTO.builder()
                    .isCorrect(playerAnswer.isCorrect())
                    .score(playerAnswer.getScore())
                    .timeTaken(playerAnswer.getAnswerTime())
                    .build();
        }
    }

    // Chuyển đổi từ entity sang DTO
    public static QuestionResultDTO fromEntity(
            Question question,
            List<Long> correctOptionIds,
            List<OptionStatDTO> optionStats,
            List<UserAnswerDTO> userAnswer
    ) {
        return QuestionResultDTO.builder()
                .questionId(question.getId())
                .correctOptions(correctOptionIds)
                .explanation(question.getExplanation())
                .funFact(question.getFunFact())
                .optionStats(optionStats)
                .userAnswer(userAnswer)
                .build();
    }
}
