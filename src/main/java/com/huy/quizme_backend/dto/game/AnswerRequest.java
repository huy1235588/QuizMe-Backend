package com.huy.quizme_backend.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO dùng để client gửi câu trả lời lên server
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {
    private Long questionId;
    private Double answerTime;
    private List<Long> selectedOptions;
    private String textAnswer;
}
