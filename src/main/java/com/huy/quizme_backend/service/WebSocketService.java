package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.game.GameResultDTO;
import com.huy.quizme_backend.dto.game.LeaderboardDTO;
import com.huy.quizme_backend.dto.game.QuestionGameDTO;
import com.huy.quizme_backend.dto.game.QuestionResultDTO;
import com.huy.quizme_backend.dto.response.ChatMessageResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service chuyên xử lý và quản lý các kênh WebSocket
 * Tập trung việc gửi tin nhắn WebSocket để các service khác không phải phụ thuộc trực tiếp
 * vào các chi tiết triển khai của WebSocket
 */
@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    // Constants cho các topic WebSocket
    public static final String ROOM_TOPIC_PREFIX = "/topic/room/";
    public static final String CHAT_EVENT = "/chat";
    public static final String GAME_START_EVENT = "/start";
    public static final String GAME_CLOSE_EVENT = "/close";
    public static final String PLAYER_JOIN_EVENT = "/player-join";
    public static final String PLAYER_LEAVE_EVENT = "/player-leave";
    public static final String GAME_PROGRESS_EVENT = "/progress";
    public static final String GAME_END_EVENT = "/end";

    // Thêm constants cho các sự kiện chơi quiz
    public static final String QUESTION_EVENT = "/question";
    public static final String TIMER_EVENT = "/timer";
    public static final String QUESTION_RESULT_EVENT = "/question-result";
    public static final String LEADERBOARD_EVENT = "/leaderboard";
    public static final String NEXT_QUESTION_EVENT = "/next-question";

    /**
     * WebSocket message container chuẩn hóa
     *
     * @param <T> Kiểu dữ liệu của payload
     */
    @Data
    @AllArgsConstructor
    public static class WebSocketMessage<T> {
        private String type;
        private T payload;
        private Instant timestamp = Instant.now();

        public WebSocketMessage(String type, T payload) {
            this.type = type;
            this.payload = payload;
        }
    }

    /**
     * Tạo đường dẫn destination cho một phòng và sự kiện
     */
    private String buildDestination(Long roomId, String eventType) {
        Assert.notNull(roomId, "Room ID không được để trống");
        return ROOM_TOPIC_PREFIX + roomId + eventType;
    }

    /**
     * Gửi message có cấu trúc chuẩn đến destination
     */
    private <T> void sendMessage(String destination, String eventType, T payload) {
        Assert.notNull(payload, "Payload không được để trống");
        WebSocketMessage<T> message = new WebSocketMessage<>(eventType, payload);
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Gửi tin nhắn trò chuyện đến một phòng cụ thể
     */
    public void sendChatMessage(Long roomId, ChatMessageResponse message) {
        String destination = buildDestination(roomId, CHAT_EVENT);
        sendMessage(destination, "CHAT", message);
    }

    /**
     * Gửi sự kiện bắt đầu trò chơi đến một phòng cụ thể
     */
    public <T> void sendGameStartEvent(Long roomId, T payload) {
        String destination = buildDestination(roomId, GAME_START_EVENT);
        sendMessage(destination, "GAME_START", payload);
    }

    /**
     * Gửi sự kiện đóng trò chơi đến một phòng cụ thể
     */
    public <T> void sendGameCloseEvent(Long roomId, T payload) {
        String destination = buildDestination(roomId, GAME_CLOSE_EVENT);
        sendMessage(destination, "GAME_CLOSE", payload);
    }

    /**
     * Gửi sự kiện người chơi tham gia phòng
     */
    public <T> void sendPlayerJoinEvent(Long roomId, T payload) {
        String destination = buildDestination(roomId, PLAYER_JOIN_EVENT);
        sendMessage(destination, "PLAYER_JOIN", payload);
    }

    /**
     * Gửi sự kiện người chơi rời phòng
     */
    public <T> void sendPlayerLeaveEvent(Long roomId, T payload) {
        String destination = buildDestination(roomId, PLAYER_LEAVE_EVENT);
        sendMessage(destination, "PLAYER_LEAVE", payload);
    }

    /**
     * Gửi sự kiện cập nhật tiến trình trò chơi
     */
    public <T> void sendGameProgressEvent(Long roomId, T payload) {
        String destination = buildDestination(roomId, GAME_PROGRESS_EVENT);
        sendMessage(destination, "GAME_PROGRESS", payload);
    }

    /**
     * Gửi sự kiện kết thúc trò chơi
     */
    public <T> void sendGameEndEvent(Long roomId, T payload) {
        String destination = buildDestination(roomId, GAME_END_EVENT);
        sendMessage(destination, "GAME_END", payload);
    }

    /**
     * Gửi sự kiện tùy chỉnh đến một phòng cụ thể
     */
    public <T> void sendRoomEvent(Long roomId, String eventType, T payload) {
        String formattedEventType = eventType.startsWith("/") ? eventType : "/" + eventType;
        String destination = buildDestination(roomId, formattedEventType);
        String eventName = eventType.replaceAll("/", "").toUpperCase();
        sendMessage(destination, eventName, payload);
    }

    /**
     * Gửi câu hỏi tới người chơi trong phòng
     *
     * @param roomId      ID phòng
     * @param questionDTO Dữ liệu câu hỏi
     */
    public void sendQuestionEvent(Long roomId, QuestionGameDTO questionDTO) {
        Assert.notNull(roomId, "RoomId không được null");
        Assert.notNull(questionDTO, "QuestionGameDTO không được null");

        // Gửi tin nhắn đến topic phòng với sự kiện câu hỏi
        String destination = buildDestination(roomId, QUESTION_EVENT);
        sendMessage(destination, "QUESTION", questionDTO);
    }

    /**
     * Gửi cập nhật thời gian đếm ngược
     *
     * @param roomId           ID phòng
     * @param remainingSeconds Số giây còn lại
     * @param totalTime        Tổng thời gian
     */
    public void sendTimerEvent(Long roomId, int remainingSeconds, int totalTime) {
        Assert.notNull(roomId, "RoomId không được null");

        Map<String, Integer> timerData = new HashMap<>();
        timerData.put("seconds", remainingSeconds);
        timerData.put("totalTime", totalTime);

        // Gửi tin nhắn đến topic phòng với sự kiện đếm ngược
        String destination = buildDestination(roomId, TIMER_EVENT);
        sendMessage(destination, "TIMER", timerData);
    }

    /**
     * Gửi kết quả câu hỏi
     *
     * @param roomId    ID phòng
     * @param resultDTO Dữ liệu kết quả
     */
    public void sendQuestionResultEvent(Long roomId, QuestionResultDTO resultDTO) {
        Assert.notNull(roomId, "RoomId không được null");
        Assert.notNull(resultDTO, "QuestionResultDTO không được null");

        // Gửi tin nhắn đến topic phòng với sự kiện kết quả câu hỏi
        String destination = buildDestination(roomId, QUESTION_RESULT_EVENT);
        sendMessage(destination, "QUESTION_RESULT", resultDTO);
    }

    /**
     * Gửi bảng xếp hạng
     *
     * @param roomId         ID phòng
     * @param leaderboardDTO Dữ liệu bảng xếp hạng
     */
    public void sendLeaderboardEvent(Long roomId, LeaderboardDTO leaderboardDTO) {
        Assert.notNull(roomId, "RoomId không được null");
        Assert.notNull(leaderboardDTO, "LeaderboardDTO không được null");

        // Gửi tin nhắn đến topic phòng với sự kiện bảng xếp hạng
        String destination = buildDestination(roomId, LEADERBOARD_EVENT);
        sendMessage(destination, "LEADERBOARD", leaderboardDTO);
    }

    /**
     * Gửi thông báo câu hỏi tiếp theo
     *
     * @param roomId             ID phòng
     * @param nextQuestionNumber Số thứ tự câu hỏi tiếp theo
     */
    public void sendNextQuestionEvent(Long roomId, int nextQuestionNumber) {
        Assert.notNull(roomId, "RoomId không được null");

        Map<String, Integer> nextQuestionData = new HashMap<>();
        nextQuestionData.put("questionNumber", nextQuestionNumber);
        nextQuestionData.put("countdown", 5); // Đếm ngược 5 giây trước câu hỏi tiếp theo

        // Gửi tin nhắn đến topic phòng với sự kiện câu hỏi tiếp theo
        String destination = buildDestination(roomId, NEXT_QUESTION_EVENT);
        sendMessage(destination, "NEXT_QUESTION", nextQuestionData);
    }

    /**
     * Gửi kết quả cuối cùng khi kết thúc trò chơi
     *
     * @param roomId    ID phòng
     * @param resultDTO Dữ liệu kết quả cuối cùng
     */
    public void sendGameEndEvent(Long roomId, GameResultDTO resultDTO) {
        Assert.notNull(roomId, "RoomId không được null");
        Assert.notNull(resultDTO, "GameResultDTO không được null");

        // Gửi tin nhắn đến topic phòng với sự kiện kết thúc trò chơi
        String destination = buildDestination(roomId, GAME_END_EVENT);
        sendMessage(destination, "GAME_END", resultDTO);
    }
}
