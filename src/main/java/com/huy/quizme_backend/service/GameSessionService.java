package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.game.AnswerRequest;
import com.huy.quizme_backend.dto.game.GameResultDTO;
import com.huy.quizme_backend.dto.game.GameStateDTO;
import com.huy.quizme_backend.enity.GamePlayerAnswer;
import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.repository.QuestionRepository;
import com.huy.quizme_backend.repository.QuizRepository;
import com.huy.quizme_backend.session.GameSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Quản lý vòng đời và trạng thái phiên chơi game.
 */
@Service
@RequiredArgsConstructor
public class GameSessionService {
    private final WebSocketService webSocketService;
    private final GameProgressService gameProgressService;
    private final GameResultService gameResultService;
    private final RoomService roomService;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    // Lưu trữ trạng thái các phiên chơi in-memory
    private final ConcurrentMap<Long, Object> sessions = new ConcurrentHashMap<>();

    /**
     * Khởi tạo phiên chơi mới.
     */
    public GameSession initGameSession(Long roomId, Long quizId) {
        throw new UnsupportedOperationException("Chưa triển khai GameSession");
    }

    /**
     * Bắt đầu trò chơi trong phòng.
     */
    public boolean startGame(Long roomId) {
        throw new UnsupportedOperationException("Chưa triển khai startGame");
    }

    /**
     * Bắt đầu câu hỏi mới.
     */
    public void startQuestion(Long roomId, int questionIndex) {
        throw new UnsupportedOperationException("Chưa triển khai startQuestion");
    }

    /**
     * Xử lý khi người chơi gửi câu trả lời.
     */
    public boolean processAnswerSubmission(Long roomId, Long userId, AnswerRequest answer) {
        throw new UnsupportedOperationException("Chưa triển khai processAnswerSubmission");
    }

    /**
     * Kết thúc câu hỏi hiện tại và gửi kết quả.
     */
    public void endCurrentQuestion(Long roomId) {
        throw new UnsupportedOperationException("Chưa triển khai endCurrentQuestion");
    }

    /**
     * Đếm ngược thời gian trước câu hỏi tiếp theo.
     */
    public void startNextQuestionCountdown(Long roomId, int seconds) {
        throw new UnsupportedOperationException("Chưa triển khai startNextQuestionCountdown");
    }

    /**
     * Kết thúc trò chơi và trả về kết quả.
     */
    public GameResultDTO endGame(Long roomId) {
        throw new UnsupportedOperationException("Chưa triển khai endGame");
    }

    /**
     * Xử lý khi người chơi kết nối lại.
     */
    public GameStateDTO reconnectPlayer(Long roomId, Long userId, String sessionId) {
        throw new UnsupportedOperationException("Chưa triển khai reconnectPlayer");
    }

    /**
     * Xử lý khi người chơi mất kết nối.
     */
    public void disconnectPlayer(Long roomId, Long userId) {
        throw new UnsupportedOperationException("Chưa triển khai disconnectPlayer");
    }

    /**
     * Lấy trạng thái hiện tại của phiên chơi.
     */
    public GameStateDTO getGameState(Long roomId) {
        throw new UnsupportedOperationException("Chưa triển khai getGameState");
    }

    /**
     * Lấy thời gian còn lại của câu hỏi đang diễn ra.
     */
    public int getRemainingTime(Long roomId) {
        throw new UnsupportedOperationException("Chưa triển khai getRemainingTime");
    }

    /**
     * Kiểm tra xem trò chơi còn đang hoạt động.
     */
    public boolean isGameActive(Long roomId) {
        throw new UnsupportedOperationException("Chưa triển khai isGameActive");
    }
}
