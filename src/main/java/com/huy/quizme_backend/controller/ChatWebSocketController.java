package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.request.ChatMessageRequest;
import com.huy.quizme_backend.dto.response.ChatMessageResponse;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Controller xử lý các tin nhắn chat qua WebSocket
 * Nhận tin nhắn từ client và gửi đến tất cả người dùng trong phòng
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * Endpoint WebSocket để gửi tin nhắn chat
     * Client sẽ gửi đến /app/chat/{roomId}
     *
     * @param roomId ID của phòng
     * @param chatRequest Yêu cầu tin nhắn chat
     * @param headerAccessor Trình truy cập header để lấy thông tin người dùng
     * @return Phản hồi tin nhắn chat (sẽ được phát qua WebSocketService)
     */
    @MessageMapping("/chat/{roomId}")
    public ChatMessageResponse sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageRequest chatRequest,
            Principal principal
    ) {
        // Đảm bảo roomId từ URL được sử dụng
        chatRequest.setRoomId(roomId);
        
        // Lấy userId từ header
        // Nếu không có người dùng đăng nhập, userId sẽ là null
        Long userId = null;
        if (principal != null) {
            User currentUser = (User) ((Authentication) principal).getPrincipal();
            userId = currentUser.getId();
        }

        // Xử lý tin nhắn thông qua ChatService
        // Chat service sẽ lưu tin nhắn và phát qua WebSocketService
        return chatService.sendMessage(chatRequest, userId);
    }
}
