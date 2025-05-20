package com.huy.quizme_backend.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;

/**
 * Service quản lý các WebSocket session và xử lý các trường hợp disconnect
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketSessionRegistry {
    private final RoomService roomService;

    // Thời gian chờ trước khi xử lý timeout (giây)
    @Value("${app.websocket.disconnect-timeout-seconds}")
    private int disconnectTimeoutSeconds;

    // Executor để lập lịch các task timeout
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    // Map lưu trữ các session ID và thông tin người dùng
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    // Map lưu trữ các task timeout đã lên lịch
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * Thông tin về một session
     */
    private static class SessionInfo {
        String sessionId;
        Long userId;              // Có thể null nếu là khách
        String guestName;         // Có thể null nếu là người dùng đăng nhập
        Set<Long> roomIds;        // Các phòng mà người dùng đang tham gia
        Instant lastDisconnectTime; // Thời điểm ngắt kết nối gần nhất

        public SessionInfo(String sessionId, Long userId, String guestName) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.guestName = guestName;
            this.roomIds = ConcurrentHashMap.newKeySet();
        }
    }

    /**
     * Đăng ký session mới khi người dùng kết nối
     */
    public void registerSession(String sessionId, Long userId, String guestName) {
        log.info("Registering session: {}, userId: {}, guestName: {}", sessionId, userId, guestName);

        // Nếu đã có task timeout cho session này, hủy nó
        cancelDisconnectTimeout(sessionId);

        // Đăng ký session
        sessions.putIfAbsent(sessionId, new SessionInfo(sessionId, userId, guestName));
    }

    /**
     * Đăng ký người dùng tham gia phòng
     */
    public void registerRoomParticipation(String sessionId, Long roomId) {
        log.info("Registering room participation: session {}, roomId: {}", sessionId, roomId);
        SessionInfo info = sessions.get(sessionId);
        if (info != null) {
            info.roomIds.add(roomId);
        }
    }

    /**
     * Xử lý khi người dùng ngắt kết nối
     */
    public void handleDisconnect(String sessionId) {
        log.info("Handling disconnect for session: {}", sessionId);
        SessionInfo info = sessions.get(sessionId);
        if (info != null) {
            info.lastDisconnectTime = Instant.now();

            // Lên lịch task để xử lý sau khoảng thời gian timeout
            scheduleDisconnectTimeout(sessionId);
        }
    }

    /**
     * Lên lịch task xử lý timeout sau khi disconnect
     */
    private void scheduleDisconnectTimeout(String sessionId) {
        log.info("Scheduling disconnect timeout for session: {}", sessionId);
        ScheduledFuture<?> task = executorService.schedule(() -> {
            processDisconnectTimeout(sessionId);
        }, disconnectTimeoutSeconds, TimeUnit.SECONDS);

        scheduledTasks.put(sessionId, task);
    }

    /**
     * Hủy task xử lý timeout (khi người dùng kết nối lại)
     */
    private void cancelDisconnectTimeout(String sessionId) {
        ScheduledFuture<?> task = scheduledTasks.remove(sessionId);
        if (task != null && !task.isDone()) {
            log.info("Canceling disconnect timeout for session: {}", sessionId);
            task.cancel(true);
        }
    }

    /**
     * Xử lý khi timeout xảy ra (người dùng không kết nối lại trong khoảng thời gian cho phép)
     */
    private void processDisconnectTimeout(String sessionId) {
        log.info("Processing disconnect timeout for session: {}", sessionId);
        SessionInfo info = sessions.get(sessionId);
        if (info != null) {
            // Xử lý từng phòng mà người dùng đang tham gia
            for (Long roomId : info.roomIds) {
                try {
                    // Nếu là người dùng đăng nhập
                    if (info.userId != null) {
                        log.info("User {} timed out from room {}", info.userId, roomId);
                        roomService.handleUserDisconnectTimeout(roomId, info.userId);
                    }
                    // Nếu là khách
                    else if (info.guestName != null) {
                        log.info("Guest {} timed out from room {}", info.guestName, roomId);
                        roomService.handleGuestDisconnectTimeout(roomId, info.guestName);
                    }
                } catch (Exception e) {
                    log.error("Error processing disconnect timeout for session {} in room {}", sessionId, roomId, e);
                }
            }

            // Xóa session sau khi đã xử lý
            sessions.remove(sessionId);
            scheduledTasks.remove(sessionId);
        }
    }

    /**
     * Xóa người dùng khỏi phòng khi rời phòng chủ động
     */
    public void removeFromRoom(String sessionId, Long roomId) {
        SessionInfo info = sessions.get(sessionId);
        if (info != null) {
            info.roomIds.remove(roomId);
        }
    }

    /**
     * Lấy userId từ sessionId
     */
    public Long getUserIdBySessionId(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        return info != null ? info.userId : null;
    }

    /**
     * Lấy sessionId từ userId
     */
    public String getSessionIdByUserId(Long userId) {
        for (Map.Entry<String, SessionInfo> entry : sessions.entrySet()) {
            if (entry.getValue().userId != null && entry.getValue().userId.equals(userId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Lấy guestName từ sessionId
     */
    public String getGuestNameBySessionId(String sessionId) {
        SessionInfo info = sessions.get(sessionId);
        return info != null ? info.guestName : null;
    }
}
