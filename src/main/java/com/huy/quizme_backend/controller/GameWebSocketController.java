package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.game.AnswerRequest;
import com.huy.quizme_backend.service.GameSessionService;
import com.huy.quizme_backend.service.GameProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Controller xử lý các sự kiện WebSocket liên quan tới game.
 */
@Controller
@RequiredArgsConstructor
public class GameWebSocketController {
    private final GameSessionService gameSessionService;
    private final GameProgressService gameProgressService;

    /**
     * Xử lý khi người chơi sẵn sàng bắt đầu.
     */
    @MessageMapping("/room/{roomId}/ready")
    public void handlePlayerReady(@DestinationVariable Long roomId, Principal principal) {
        throw new UnsupportedOperationException("Chưa triển khai handlePlayerReady");
    }

    /**
     * Xử lý khi người chơi gửi câu trả lời.
     */
    @MessageMapping("/room/{roomId}/answer")
    public void handlePlayerAnswer(@DestinationVariable Long roomId,
                                   @Payload AnswerRequest answer,
                                   Principal principal) {
        throw new UnsupportedOperationException("Chưa triển khai handlePlayerAnswer");
    }
}
