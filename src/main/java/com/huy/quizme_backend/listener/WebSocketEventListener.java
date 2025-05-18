package com.huy.quizme_backend.listener;

import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.service.WebSocketSessionRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lắng nghe các sự kiện WebSocket và xử lý kết nối/ngắt kết nối
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final WebSocketSessionRegistry sessionRegistry;

    // Pattern để trích xuất roomId từ destination topic
    private static final Pattern ROOM_TOPIC_PATTERN = Pattern.compile("/topic/room/(\\d+)(/.*)?");

    /**
     * Xử lý khi người dùng kết nối WebSocket
     */
    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        log.info("Client connecting: {}", sessionId);

        // Xác định user hoặc guest
        Long userId = null;
        String guestName = null;

        // Nếu người dùng đã đăng nhập, lấy thông tin từ authentication
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
            if (auth.getPrincipal() instanceof User) {
                User user = (User) auth.getPrincipal();
                userId = user.getId();
                log.info("Authenticated user connected: {}", user.getUsername());
            }
        }

        // Đăng ký session
        sessionRegistry.registerSession(sessionId, userId, guestName);
    }

    /**
     * Xử lý khi người dùng đăng ký theo dõi một topic
     * Sử dụng để phát hiện người dùng tham gia phòng
     */
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        if (destination != null) {
            // Kiểm tra xem destination có phải là topic phòng không
            Matcher matcher = ROOM_TOPIC_PATTERN.matcher(destination);
            if (matcher.matches()) {
                String roomIdStr = matcher.group(1);
                try {
                    Long roomId = Long.parseLong(roomIdStr);
                    // Đăng ký người dùng tham gia phòng này
                    sessionRegistry.registerRoomParticipation(sessionId, roomId);
                    log.info("User subscribed to room {}: sessionId={}, destination={}",
                            roomId, sessionId, destination);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse roomId from destination: {}", destination, e);
                }
            }
        }
    }

    /**
     * Xử lý khi người dùng ngắt kết nối WebSocket
     */
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        log.info("Client disconnected: {}", sessionId);

        // Xử lý ngắt kết nối
        sessionRegistry.handleDisconnect(sessionId);
    }
}
