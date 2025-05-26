package com.huy.quizme_backend.session;

import com.huy.quizme_backend.dto.game.QuestionGameDTO;
import com.huy.quizme_backend.enity.Question;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mô hình phiên chơi game lưu trữ trong bộ nhớ.
 */
@Data
@NoArgsConstructor
public class GameSession {
    private Long roomId;
    private Long quizId;
    private GameStatus status;
    private Integer currentQuestionIndex;
    private ConcurrentMap<Long, ParticipantSession> participants = new ConcurrentHashMap<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<QuestionGameDTO> questions;
    private ScheduledFuture<?> currentTimer;
    private ScheduledFuture<?> endTimer;
}
