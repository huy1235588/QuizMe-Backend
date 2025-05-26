package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.game.AnswerRequest;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.service.GameSessionService;
import com.huy.quizme_backend.service.GameProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Controller xử lý các sự kiện WebSocket liên quan tới game.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class GameWebSocketController {
    private final GameSessionService gameSessionService;
    private final GameProgressService gameProgressService;

    /**
     * Xử lý khi người chơi gửi câu trả lời.
     */
    @MessageMapping("/answer/{roomId}")
    public void handlePlayerAnswer(
            @DestinationVariable Long roomId,
            @Payload AnswerRequest answer,
            Principal principal
    ) {
        // Lấy userId từ header
        // Nếu không có người dùng đăng nhập, userId sẽ là null
        Long userId = null;
        if (principal != null) {
            User currentUser = (User) ((Authentication) principal).getPrincipal();
            userId = currentUser.getId();
        }

        // Xử lý câu trả lời thông qua GameSessionService
        boolean success = gameSessionService.processAnswerSubmission(roomId, userId, answer);

        if (!success) {
            log.warn("Failed to process answer submission for user {} in room {}", userId, roomId);
        }
    }
}
