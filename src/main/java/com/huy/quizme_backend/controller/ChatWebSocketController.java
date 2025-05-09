package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.request.ChatMessageRequest;
import com.huy.quizme_backend.dto.response.ChatMessageResponse;
import com.huy.quizme_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * Endpoint WebSocket để gửi tin nhắn chat
     * Client nên gửi đến /app/chat.sendMessage
     *
     * @param chatRequest Yêu cầu tin nhắn chat
     * @param headerAccessor Trình truy cập header để lấy thông tin người dùng
     * @return Phản hồi tin nhắn chat (sẽ được phát qua service)
     */
    @MessageMapping("/chat.sendMessage")
    public ChatMessageResponse sendMessage(
            @Payload ChatMessageRequest chatRequest,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // Lấy principal của người dùng từ phiên WebSocket
        Principal principal = headerAccessor.getUser();
        Long userId = null;
        
        // Kiểm tra xem người dùng đã được xác thực hay chưa
        if (principal != null) {
            // Trích xuất ID người dùng từ principal
            userId = Long.parseLong(principal.getName());
        }

        // Xử lý tin nhắn
        return chatService.sendMessage(chatRequest, userId);
    }
}