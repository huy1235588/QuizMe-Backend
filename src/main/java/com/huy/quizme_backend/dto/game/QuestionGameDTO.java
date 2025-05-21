package com.huy.quizme_backend.dto.game;

import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.QuestionOption;
import com.huy.quizme_backend.enity.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

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

        // Chuyển đổi từ entity sang DTO
        public static QuestionOptionDTO fromEntity(QuestionOption option) {
            return QuestionOptionDTO.builder()
                    .id(option.getId())
                    .content(option.getContent())
                    .build();
        }

        // Chuyển đổi từ danh sách entity sang danh sách DTO
        public static List<QuestionOptionDTO> fromEntityList(List<QuestionOption> options) {
            return options.stream()
                    .map(QuestionOptionDTO::fromEntity)
                    .collect(Collectors.toList());
        }
    }

    // Chuyển đổi từ entity sang DTO
    public static QuestionGameDTO fromEntity(Question question) {
        return QuestionGameDTO.builder()
                .questionId(question.getId())
                .content(question.getContent())
                .imageUrl(question.getImageUrl())
                .audioUrl(question.getAudioUrl())
                .videoUrl(question.getVideoUrl())
                .type(question.getType())
                .timeLimit(question.getTimeLimit())
                .points(question.getPoints())
                .build();
    }

    // Chuyển đổi từ entity sang DTO với danh sách lựa chọn
    public static QuestionGameDTO fromEntityWithOptions(
            Question question,
            List<QuestionOptionDTO> options
    ) {
        return QuestionGameDTO.builder()
                .questionId(question.getId())
                .content(question.getContent())
                .imageUrl(question.getImageUrl())
                .audioUrl(question.getAudioUrl())
                .videoUrl(question.getVideoUrl())
                .type(question.getType())
                .timeLimit(question.getTimeLimit())
                .points(question.getPoints())
                .options(options)
                .build();
    }

    // Chuyển đổi từ entity sang DTO với danh sách lựa chọn và số thứ tự
    public static QuestionGameDTO fromEntityWithOptions(
            Question question,
            List<QuestionOption> options,
            Integer questionNumber,
            Integer totalQuestions
    ) {
        List<QuestionOptionDTO> optionDTOs = QuestionOptionDTO.fromEntityList(options);

        return QuestionGameDTO.builder()
                .questionId(question.getId())
                .content(question.getContent())
                .imageUrl(question.getImageUrl())
                .audioUrl(question.getAudioUrl())
                .videoUrl(question.getVideoUrl())
                .type(question.getType())
                .timeLimit(question.getTimeLimit())
                .points(question.getPoints())
                .questionNumber(questionNumber)
                .totalQuestions(totalQuestions)
                .options(optionDTOs)
                .build();
    }

    // Chuyển đổi từ DTO sang entity
    public Question toEntity() {
        Question question = new Question();
        question.setId(this.questionId);
        question.setContent(this.content);
        question.setImageUrl(this.imageUrl);
        question.setAudioUrl(this.audioUrl);
        question.setVideoUrl(this.videoUrl);
        question.setType(this.type);
        question.setTimeLimit(this.timeLimit);
        question.setPoints(this.points);
        return question;
    }
}
