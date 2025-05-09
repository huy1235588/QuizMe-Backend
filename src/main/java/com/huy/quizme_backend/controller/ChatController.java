package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.request.ChatMessageRequest;
import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.ChatMessageResponse;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Lấy lịch sử chat của một phòng
     *
     * @param roomId ID của phòng
     * @return Danh sách các tin nhắn chat
     */
    @GetMapping("/room/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ChatMessageResponse>> getChatHistory(@PathVariable Long roomId) {
        List<ChatMessageResponse> chatHistory = chatService.getChatHistory(roomId);
        return ApiResponse.success(chatHistory, "Chat history retrieved successfully");
    }

    /**
     * Gửi tin nhắn chat thông qua REST API
     *
     * @param chatRequest Yêu cầu gửi tin nhắn chat
     * @param principal   Principal của người dùng (có thể là null đối với khách)
     * @return Phản hồi tin nhắn chat
     */
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ChatMessageResponse> sendMessage(
            @Valid @RequestBody ChatMessageRequest chatRequest,
            Principal principal
    ) {
        // Lấy ID người dùng nếu người dùng đã đăng nhập
        Long userId = null;
        if (principal != null) {
            User currentUser = (User) ((Authentication) principal).getPrincipal();
            userId = currentUser.getId();
        }

        ChatMessageResponse message = chatService.sendMessage(chatRequest, userId);
        return ApiResponse.created(message, "Message sent successfully");
    }
}