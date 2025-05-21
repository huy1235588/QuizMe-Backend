package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.game.AnswerRequest;
import com.huy.quizme_backend.dto.game.GameResultDTO;
import com.huy.quizme_backend.dto.game.LeaderboardDTO;
import com.huy.quizme_backend.dto.game.QuestionGameDTO;
import com.huy.quizme_backend.dto.game.QuestionResultDTO;
import com.huy.quizme_backend.enity.GameResult;
import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.Quiz;
import com.huy.quizme_backend.repository.QuestionOptionRepository;
import com.huy.quizme_backend.repository.QuestionRepository;
import com.huy.quizme_backend.repository.QuizRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Xử lý tiến trình trò chơi: câu hỏi, câu trả lời, tính điểm, bảng xếp hạng
 */
@Service
@RequiredArgsConstructor
public class GameProgressService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final GameResultService gameResultService;

    /**
     * Tải quiz và câu hỏi
     */
    public List<Question> loadQuiz(Long quizId) {
        throw new UnsupportedOperationException("Chưa triển khai loadQuiz");
    }

    /**
     * Chuyển Question thành QuestionGameDTO
     */
    public QuestionGameDTO prepareQuestion(Question question) {
        throw new UnsupportedOperationException("Chưa triển khai prepareQuestion");
    }

    /**
     * Kiểm tra câu trả lời
     */
    public boolean validateAnswer(Question question, AnswerRequest answerRequest) {
        throw new UnsupportedOperationException("Chưa triển khai validateAnswer");
    }

    /**
     * Tính điểm dựa trên độ chính xác và thời gian
     */
    public int calculateScore(Question question, AnswerRequest answerRequest, int timeRemaining) {
        throw new UnsupportedOperationException("Chưa triển khai calculateScore");
    }

    /**
     * Tính kết quả cho một câu hỏi
     */
    public QuestionResultDTO calculateResults(Object session, int questionIndex) {
        throw new UnsupportedOperationException("Chưa triển khai calculateResults");
    }

    /**
     * Cập nhật bảng xếp hạng
     */
    public LeaderboardDTO updateLeaderboard(Object session) {
        throw new UnsupportedOperationException("Chưa triển khai updateLeaderboard");
    }

    /**
     * Hoàn thiện kết quả trò chơi
     */
    public GameResult finalizeResults(Object session) {
        throw new UnsupportedOperationException("Chưa triển khai finalizeResults");
    }

    /**
     * Lưu kết quả trò chơi
     */
    public void saveGameResults(Object session, GameResult gameResult) {
        throw new UnsupportedOperationException("Chưa triển khai saveGameResults");
    }
}
