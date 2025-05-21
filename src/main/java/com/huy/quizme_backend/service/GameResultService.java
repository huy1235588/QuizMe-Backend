package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.game.GameResultDTO;
import com.huy.quizme_backend.enity.GameResult;
import com.huy.quizme_backend.repository.GameResultQuestionRepository;
import com.huy.quizme_backend.repository.GameResultRepository;
import com.huy.quizme_backend.repository.GamePlayerAnswerOptionRepository;
import com.huy.quizme_backend.repository.GamePlayerAnswerRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Quản lý và truy vấn kết quả trò chơi
 */
@Service
@RequiredArgsConstructor
public class GameResultService {
    private final GameResultRepository gameResultRepository;
    private final GameResultQuestionRepository resultQuestionRepository;
    private final GamePlayerAnswerRepository playerAnswerRepository;
    private final GamePlayerAnswerOptionRepository answerOptionRepository;

    /**
     * Lưu kết quả trò chơi
     */
    public GameResult saveGameResult(GameResult gameResult) {
        throw new UnsupportedOperationException("Chưa triển khai saveGameResult");
    }

    /**
     * Lấy kết quả trò chơi
     */
    public GameResultDTO getGameResult(Long roomId) {
        throw new UnsupportedOperationException("Chưa triển khai getGameResult");
    }

    /**
     * Lấy thống kê người chơi
     */
    public Object getPlayerStatistics(Long userId) {
        throw new UnsupportedOperationException("Chưa triển khai getPlayerStatistics");
    }

    /**
     * Lấy thống kê câu hỏi
     */
    public Object getQuestionStatistics(Long questionId) {
        throw new UnsupportedOperationException("Chưa triển khai getQuestionStatistics");
    }

    /**
     * Lấy bảng xếp hạng chung của quiz
     */
    public List<Object> getLeaderboard(Long quizId, int limit) {
        throw new UnsupportedOperationException("Chưa triển khai getLeaderboard");
    }
}
