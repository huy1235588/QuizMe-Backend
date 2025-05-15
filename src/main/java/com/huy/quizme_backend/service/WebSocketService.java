package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.response.ChatMessageResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;

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
    public static final String PLAYER_JOIN_EVENT = "/player-join";
    public static final String PLAYER_LEAVE_EVENT = "/player-leave";
    public static final String GAME_PROGRESS_EVENT = "/progress";
    public static final String GAME_END_EVENT = "/end";
    
    /**
     * WebSocket message container chuẩn hóa
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
}
