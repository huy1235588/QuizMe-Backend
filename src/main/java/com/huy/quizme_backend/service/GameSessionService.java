package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.game.AnswerRequest;
import com.huy.quizme_backend.dto.game.GameResultDTO;
import com.huy.quizme_backend.dto.game.GameStateDTO;
import com.huy.quizme_backend.dto.game.LeaderboardDTO;
import com.huy.quizme_backend.dto.game.QuestionGameDTO;
import com.huy.quizme_backend.dto.game.QuestionResultDTO;
import com.huy.quizme_backend.enity.GamePlayerAnswer;
import com.huy.quizme_backend.enity.GameResult;
import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.RoomParticipant;
import com.huy.quizme_backend.repository.QuestionRepository;
import com.huy.quizme_backend.repository.QuizRepository;
import com.huy.quizme_backend.repository.RoomParticipantRepository;
import com.huy.quizme_backend.repository.RoomRepository;
import com.huy.quizme_backend.session.ConnectionStatus;
import com.huy.quizme_backend.session.GameSession;
import com.huy.quizme_backend.session.GameStatus;
import com.huy.quizme_backend.session.ParticipantSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Quản lý vòng đời và trạng thái phiên chơi game.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameSessionService {
    private final WebSocketService webSocketService;
    private final LocalStorageService localStorageService;
    private final GameProgressService gameProgressService;
    private final GameResultService gameResultService;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    // Lưu trữ trạng thái các phiên chơi in-memory
    private final ConcurrentMap<Long, GameSession> sessions = new ConcurrentHashMap<>();

    // Thread pool để quản lý các timer
    private final ScheduledExecutorService timerExecutor = Executors.newScheduledThreadPool(10);

    /**
     * Khởi tạo phiên chơi mới.
     */
    public GameSession initGameSession(Long roomId) {
        Long quizId = roomRepository.findQuizIdById(roomId);

        // Khởi tạo phiên chơi
        GameSession gameSession = new GameSession();
        gameSession.setRoomId(roomId);
        gameSession.setQuizId(quizId);
        gameSession.setStatus(GameStatus.WAITING);

        // Lấy danh sách người chơi trong phòng
        List<RoomParticipant> participantSession = roomParticipantRepository.findByRoomId(roomId);
        // Lưu danh sách người chơi vào phiên chơi
        for (RoomParticipant participant : participantSession) {
            ParticipantSession session = new ParticipantSession();
            session.setUserId(participant.getUser().getId());
            session.setUsername(participant.getUser().getUsername());
            session.setScore(0);
            session.setRank(0);
            session.setConnectionStatus(ConnectionStatus.ACTIVE);
            gameSession.getParticipants().put(participant.getUser().getId(), session);
        }

        // Lấy danh sách câu hỏi từ quiz
        List<QuestionGameDTO> questionGameDTO = gameProgressService.loadQuizAndPrepareQuestions(quizId);
        // Lưu danh sách câu hỏi vào phiên chơi
        gameSession.setQuestions(questionGameDTO);

        // Lưu phiên chơi vào bộ nhớ
        sessions.put(roomId, gameSession);

        return gameSession;
    }

    /**
     * Bắt đầu trò chơi trong phòng.
     */
    public boolean startGame(Long roomId) {
        // Lấy phiên chơi từ bộ nhớ
        GameSession gameSession = sessions.get(roomId);
        if (gameSession == null) {
            return false; // Phiên chơi không tồn tại
        }

        // Đặt trạng thái trò chơi là ĐANG CHƠI
        gameSession.setStatus(GameStatus.IN_PROGRESS);
        // Đặt thời gian bắt đầu trò chơi
        gameSession.setStartTime(LocalDateTime.now());

        // Gửi thông báo đến tất cả người chơi
        webSocketService.sendGameStartEvent(roomId, "Trò chơi đã bắt đầu!");

        // Bắt đầu câu hỏi đầu tiên
        startQuestion(roomId, 0);

        return true;
    }

    /**
     * Bắt đầu câu hỏi mới.
     */
    public void startQuestion(Long roomId, int questionIndex) {
        GameSession gameSession = sessions.get(roomId);

        // Kiểm tra còn câu hỏi không
        if (questionIndex >= gameSession.getQuestions().size()) {
            // Không còn câu hỏi nào
            endGame(roomId);
            return;
        }

        // Cập nhật trạng thái câu hỏi
        gameSession.setCurrentQuestionIndex(questionIndex);
        gameSession.setStatus(GameStatus.IN_PROGRESS);
        gameSession.setStartTime(LocalDateTime.now());

        // Lấy câu hỏi hiện tại
        QuestionGameDTO currentQuestion = gameSession.getQuestions().get(questionIndex);

        // Cập nhật URL hình ảnh nếu có
        currentQuestion.setImageUrl(localStorageService.getQuestionImageUrl(currentQuestion.getImageUrl()));
        // Gửi câu hỏi đến tất cả người chơi
        webSocketService.sendQuestionEvent(roomId, currentQuestion);

        // Huỷ timer hiện tại nếu có
        cancelCurrentTimer(gameSession);

        log.info("Bắt đầu câu hỏi {} cho phòng {}. Thời gian: {} giây",
                questionIndex + 1, roomId, currentQuestion.getTimeLimit());

        // Bắt đầu timer đếm ngược từng giây
        startQuestionTimer(roomId, currentQuestion.getTimeLimit());
    }

    /**
     * Xử lý khi người chơi gửi câu trả lời.
     */
    public boolean processAnswerSubmission(Long roomId, Long userId, AnswerRequest answerRequest) {
        GameSession gameSession = sessions.get(roomId);
        if (gameSession == null || gameSession.getStatus() != GameStatus.IN_PROGRESS) {
            return false;
        }

        // Lấy người chơi từ phiên chơi
        ParticipantSession participantSession = gameSession.getParticipants().get(userId);
        if (participantSession == null) {
            return false;
        }

        // Kiểm tra xem người chơi đã trả lời câu hỏi này chưa
        if (participantSession.getAnswers().containsKey(answerRequest.getQuestionId())) {
            return false; // Đã trả lời rồi
        }

        // Tạo GamePlayerAnswer từ request
        GamePlayerAnswer playerAnswer = GamePlayerAnswer.builder()
                .question(questionRepository.findById(answerRequest.getQuestionId()).orElse(null))
                .answerTime(answerRequest.getAnswerTime())
                .build();

        // Lưu các lựa chọn đã chọn (cần tạo GamePlayerAnswerOption entities)
        // Note: Trong thực tế cần tạo và lưu GamePlayerAnswerOption entities
        // Hiện tại chỉ lưu IDs tạm thời

        // Lưu câu trả lời vào session
        participantSession.getAnswers().put(answerRequest.getQuestionId(), playerAnswer);

        return true;
    }

    /**
     * Kết thúc câu hỏi hiện tại và gửi kết quả.
     */
    public void endCurrentQuestion(Long roomId) {
        GameSession gameSession = sessions.get(roomId);
        if (gameSession == null || gameSession.getCurrentQuestionIndex() == null) {
            return;
        }

        log.info("Kết thúc câu hỏi {} trong phòng {}",
                gameSession.getCurrentQuestionIndex() + 1, roomId);

        // Hủy các timer hiện tại
        cancelCurrentTimer(gameSession);

        // Đặt trạng thái là kết thúc câu hỏi
        gameSession.setStatus(GameStatus.QUESTION_END);        // Lấy câu hỏi hiện tại
        QuestionGameDTO currentQuestionDTO = gameSession.getQuestions().get(gameSession.getCurrentQuestionIndex());
        // Sử dụng findByIdWithOptions để eager load options và tránh lỗi lazy loading trong timer thread
        Question currentQuestionEntity = questionRepository.findByIdWithOptions(currentQuestionDTO.getQuestionId()).orElse(null);

        if (currentQuestionEntity == null) {
            log.error("Không tìm thấy câu hỏi với ID {} trong phòng {}",
                    currentQuestionDTO.getQuestionId(), roomId);
            return;
        }

        // Tính kết quả cho câu hỏi này
        QuestionResultDTO questionResult = gameProgressService.calculateResults(gameSession, currentQuestionEntity);

        // Gửi kết quả câu hỏi đến tất cả người chơi
        webSocketService.sendQuestionResultEvent(roomId, questionResult);

        // Tạo và gửi bảng xếp hạng cập nhật
        LeaderboardDTO leaderboard = gameProgressService.generateLeaderboardDTO(gameSession);
        webSocketService.sendLeaderboardEvent(roomId, leaderboard);

        // Kiểm tra xem còn câu hỏi nào không
        int nextQuestionIndex = gameSession.getCurrentQuestionIndex() + 1;
        if (nextQuestionIndex >= gameSession.getQuestions().size()) {
            // Hết câu hỏi, kết thúc game
            log.info("Hết câu hỏi, kết thúc game trong phòng {}", roomId);
            endGame(roomId);
        } else {
            // Bắt đầu đếm ngược cho câu hỏi tiếp theo
            log.info("Bắt đầu đếm ngược 5 giây cho câu hỏi tiếp theo trong phòng {}", roomId);
            startNextQuestionCountdown(roomId, 5); // 5 giây countdown
        }
    }

    /**
     * Đếm ngược thời gian trước câu hỏi tiếp theo.
     */
    public void startNextQuestionCountdown(Long roomId, int seconds) {
        GameSession gameSession = sessions.get(roomId);
        if (gameSession == null) {
            log.warn("Không tìm thấy phiên chơi {} để bắt đầu đếm ngược", roomId);
            return;
        }

        // Đặt trạng thái đếm ngược
        gameSession.setStatus(GameStatus.NEXT_QUESTION);

        // Gửi thông báo câu hỏi tiếp theo
        int nextQuestionNumber = gameSession.getCurrentQuestionIndex() + 2; // +1 cho index, +1 cho số thứ tự
        webSocketService.sendNextQuestionEvent(roomId, nextQuestionNumber);

        log.info("Bắt đầu đếm ngược {} giây cho câu hỏi {} trong phòng {}",
                seconds, nextQuestionNumber, roomId);

        // Hủy timer hiện tại nếu có
        cancelCurrentTimer(gameSession);

        // Bắt đầu timer đếm ngược từng giây
        AtomicBoolean countdownActive = new AtomicBoolean(true);

        ScheduledFuture<?> countdownTask = timerExecutor.scheduleAtFixedRate(() -> {
            if (!countdownActive.get()) {
                return;
            }

            try {
                GameSession session = sessions.get(roomId);
                if (session == null || session.getStatus() != GameStatus.NEXT_QUESTION) {
                    countdownActive.set(false);
                    return;
                }

                // Tính thời gian đã trôi qua kể từ khi bắt đầu đếm ngược
                long elapsedTime = Duration.between(session.getStartTime(), LocalDateTime.now()).getSeconds();
                int remainingTime = Math.max(0, seconds - (int) elapsedTime);

                log.debug("Đếm ngược câu hỏi tiếp theo - Phòng {}: {} giây còn lại", roomId, remainingTime);

                // Gửi timer event với countdown
                webSocketService.sendTimerEvent(roomId, remainingTime, seconds);

                if (remainingTime <= 0) {
                    countdownActive.set(false);
                    log.info("Kết thúc đếm ngược, bắt đầu câu hỏi tiếp theo trong phòng {}", roomId);
                    int nextQuestionIndex = session.getCurrentQuestionIndex() + 1;
                    startQuestion(roomId, nextQuestionIndex);
                }

            } catch (Exception e) {
                log.error("Lỗi khi xử lý countdown cho phòng {}: {}", roomId, e.getMessage(), e);
                countdownActive.set(false);
            }
        }, 0, 1, TimeUnit.SECONDS);

        // Backup timer để đảm bảo chuyển câu hỏi
        ScheduledFuture<?> backupTask = timerExecutor.schedule(() -> {
            countdownActive.set(false);
            if (countdownTask != null && !countdownTask.isDone()) {
                countdownTask.cancel(true);
            }
            log.info("Backup countdown timer kích hoạt cho phòng {}", roomId);
            int nextQuestionIndex = gameSession.getCurrentQuestionIndex() + 1;
            startQuestion(roomId, nextQuestionIndex);
        }, seconds + 1, TimeUnit.SECONDS);

        // Cập nhật thời gian bắt đầu đếm ngược
        gameSession.setStartTime(LocalDateTime.now());
        gameSession.setCurrentTimer(countdownTask);
        gameSession.setEndTimer(backupTask);
    }

    /**
     * Kết thúc trò chơi và trả về kết quả.
     */
    public GameResultDTO endGame(Long roomId) {
        GameSession gameSession = sessions.get(roomId);
        if (gameSession == null) {
            log.warn("Không tìm thấy phiên chơi {} để kết thúc", roomId);
            return null;
        }

        log.info("Kết thúc trò chơi trong phòng {}", roomId);

        // Đặt trạng thái kết thúc
        gameSession.setStatus(GameStatus.COMPLETED);
        gameSession.setEndTime(LocalDateTime.now());

        // Huỷ tất cả timer hiện tại
        cancelCurrentTimer(gameSession);

        // Tính toán kết quả cuối cùng
        GameResult savedGameResult = gameProgressService.finalizeResults(gameSession);

        // Tạo GameResultDTO để gửi về client
        GameResultDTO resultDTO = GameResultDTO.builder()
                .roomId(roomId)
                .quizTitle(quizRepository.findById(gameSession.getQuizId()).orElse(null).getTitle())
                .totalQuestions(gameSession.getQuestions().size())
                .duration((int) java.time.Duration.between(gameSession.getStartTime(), gameSession.getEndTime()).getSeconds())
                .build();

        // Gửi kết quả cuối cùng đến tất cả người chơi
        webSocketService.sendGameEndEvent(roomId, resultDTO);

        // Xóa phiên chơi khỏi bộ nhớ
        sessions.remove(roomId);

        return resultDTO;
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
        GameSession gameSession = sessions.get(roomId);
        if (gameSession == null) {
            return GameStateDTO.inactive();
        }

        // Nếu game không hoạt động
        if (!isGameActive(roomId)) {
            return GameStateDTO.inactive();
        }

        // Lấy câu hỏi hiện tại
        QuestionGameDTO currentQuestion = null;
        if (gameSession.getCurrentQuestionIndex() != null &&
                gameSession.getCurrentQuestionIndex() < gameSession.getQuestions().size()) {
            currentQuestion = gameSession.getQuestions().get(gameSession.getCurrentQuestionIndex());
        }

        // Tạo bảng xếp hạng hiện tại
        LeaderboardDTO leaderboard = gameProgressService.generateLeaderboardDTO(gameSession);

        // Tính thời gian còn lại
        int remainingTime = getRemainingTime(roomId);

        return GameStateDTO.builder()
                .gameActive(true)
                .currentQuestion(currentQuestion)
                .remainingTime(remainingTime)
                .leaderboard(leaderboard)
                .questionNumber(gameSession.getCurrentQuestionIndex() != null ?
                        gameSession.getCurrentQuestionIndex() + 1 : null)
                .totalQuestions(gameSession.getQuestions().size())
                .build();
    }

    /**
     * Lấy thời gian còn lại của câu hỏi đang diễn ra.
     */
    public int getRemainingTime(Long roomId) {
        GameSession gameSession = sessions.get(roomId);
        if (gameSession == null ||
                gameSession.getStatus() != GameStatus.IN_PROGRESS ||
                gameSession.getCurrentQuestionIndex() == null ||
                gameSession.getStartTime() == null) {
            return 0;
        }

        // Lấy câu hỏi hiện tại
        QuestionGameDTO currentQuestion = gameSession.getQuestions().get(gameSession.getCurrentQuestionIndex());

        // Tính thời gian đã trôi qua
        long elapsedSeconds = Duration.between(gameSession.getStartTime(), LocalDateTime.now()).getSeconds();

        // Tính thời gian còn lại
        int remainingTime = currentQuestion.getTimeLimit() - (int) elapsedSeconds;

        return Math.max(0, remainingTime);
    }

    /**
     * Kiểm tra xem trò chơi còn đang hoạt động.
     */
    public boolean isGameActive(Long roomId) {
        GameSession gameSession = sessions.get(roomId);
        if (gameSession == null) {
            return false;
        }

        // Kiểm tra trạng thái game
        return gameSession.getStatus() == GameStatus.IN_PROGRESS ||
                gameSession.getStatus() == GameStatus.QUESTION_END ||
                gameSession.getStatus() == GameStatus.NEXT_QUESTION;
    }

    /**
     * Cleanup method to properly shutdown the timer executor when service is destroyed.
     */
    @PreDestroy
    public void cleanup() {
        log.info("Shutting down GameSessionService timer executor");

        // Cancel all active timers
        sessions.values().forEach(this::cancelCurrentTimer);

        // Shutdown the executor
        timerExecutor.shutdown();
        try {
            if (!timerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Timer executor did not terminate gracefully, forcing shutdown");
                timerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for timer executor to terminate", e);
            timerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("GameSessionService cleanup completed");
    }

    /**
     * Get statistics about active game sessions for monitoring.
     */
    public String getSessionStatistics() {
        int totalSessions = sessions.size();
        long activeSessions = sessions.values().stream()
                .mapToLong(session -> isGameActive(session.getRoomId()) ? 1L : 0L)
                .sum();

        return String.format("Total sessions: %d, Active sessions: %d", totalSessions, activeSessions);
    }

    /**
     * Bắt đầu timer đếm ngược từng giây cho câu hỏi.
     */
    private void startQuestionTimer(Long roomId, int totalSeconds) {
        GameSession gameSession = sessions.get(roomId);
        if (gameSession == null) {
            log.warn("Không tìm thấy phiên chơi {} để bắt đầu timer", roomId);
            return;
        }

        AtomicBoolean timerActive = new AtomicBoolean(true);

        // Lập lịch gửi timer event mỗi giây
        ScheduledFuture<?> timerTask = timerExecutor.scheduleAtFixedRate(() -> {
            if (!timerActive.get()) {
                return;
            }

            try {
                GameSession session = sessions.get(roomId);
                if (session == null || session.getStatus() != GameStatus.IN_PROGRESS) {
                    timerActive.set(false);
                    return;
                }

                int remainingTime = getRemainingTime(roomId);

                // Log mỗi 5 giây để không spam log
                if (remainingTime % 5 == 0 || remainingTime <= 5) {
                    log.debug("Phòng {}: Thời gian còn lại {} giây", roomId, remainingTime);
                }

                // Gửi timer event đến client
                webSocketService.sendTimerEvent(roomId, remainingTime, totalSeconds);

                // Kết thúc câu hỏi khi hết thời gian
                if (remainingTime <= 0) {
                    timerActive.set(false);
                    log.info("Hết thời gian cho câu hỏi trong phòng {}", roomId);
                    endCurrentQuestion(roomId);
                }

            } catch (Exception e) {
                log.error("Lỗi khi xử lý timer cho phòng {}: {}", roomId, e.getMessage(), e);
                timerActive.set(false);
            }
        }, 0, 1, TimeUnit.SECONDS);

        // Lập lịch kết thúc câu hỏi sau thời gian quy định (backup timer)
        ScheduledFuture<?> endTask = timerExecutor.schedule(() -> {
            timerActive.set(false);
            if (!timerTask.isDone()) {
                timerTask.cancel(true);
            }
            log.info("Backup timer kết thúc câu hỏi cho phòng {}", roomId);
            endCurrentQuestion(roomId);
        }, totalSeconds + 1, TimeUnit.SECONDS); // +1 giây để đảm bảo timer chính chạy trước

        // Lưu timer vào session
        gameSession.setCurrentTimer(timerTask);
        gameSession.setEndTimer(endTask);
    }

    /**
     * Hủy timer hiện tại của phiên chơi.
     */
    private void cancelCurrentTimer(GameSession gameSession) {
        if (gameSession.getCurrentTimer() != null && !gameSession.getCurrentTimer().isDone()) {
            gameSession.getCurrentTimer().cancel(true);
            log.debug("Đã hủy timer chính cho phòng {}", gameSession.getRoomId());
        }

        if (gameSession.getEndTimer() != null && !gameSession.getEndTimer().isDone()) {
            gameSession.getEndTimer().cancel(true);
            log.debug("Đã hủy backup timer cho phòng {}", gameSession.getRoomId());
        }
    }
}
