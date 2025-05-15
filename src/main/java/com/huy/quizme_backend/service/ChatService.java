package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.ChatMessageRequest;
import com.huy.quizme_backend.dto.response.ChatMessageResponse;
import com.huy.quizme_backend.enity.Room;
import com.huy.quizme_backend.enity.RoomChat;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.repository.RoomChatRepository;
import com.huy.quizme_backend.repository.RoomRepository;
import com.huy.quizme_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final RoomChatRepository roomChatRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final LocalStorageService localStorageService;
    private final WebSocketService webSocketService;

    /**
     * Lấy lịch sử trò chuyện của phòng
     *
     * @param roomId ID của phòng
     * @return Danh sách tin nhắn trò chuyện
     */
    public List<ChatMessageResponse> getChatHistory(Long roomId) {
        // Kiểm tra xem phòng có tồn tại không
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with id: " + roomId));

        // Lấy 50 tin nhắn mới nhất (sẽ được hiển thị theo thứ tự ngược trong giao diện người dùng)
        List<RoomChat> chatMessages = roomChatRepository.findTop50ByRoomIdOrderBySentAtDesc(roomId);

        // Đảo ngược để hiển thị tin nhắn cũ nhất trước
        Collections.reverse(chatMessages);

        return chatMessages.stream()
                .map(chat -> ChatMessageResponse.fromRoomChat(chat, localStorageService))
                .collect(Collectors.toList());
    }

    /**
     * Gửi một tin nhắn trò chuyện
     *
     * @param chatRequest Yêu cầu tin nhắn trò chuyện
     * @param userId      ID của người dùng gửi tin nhắn (null nếu là khách)
     * @return Phản hồi tin nhắn trò chuyện
     */
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest chatRequest, Long userId) {
        // Tìm phòng
        Room room = roomRepository.findById(chatRequest.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found with id: " + chatRequest.getRoomId()));

        // Tạo thực thể trò chuyện
        RoomChat chat = new RoomChat();
        chat.setRoom(room);
        chat.setMessage(chatRequest.getMessage());

        if (userId != null) {
            // Người dùng đã đăng nhập
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User not found with id: " + userId));
            chat.setUser(user);
            chat.setIsGuest(false);
        } else {
            // Người dùng là khách
            if (chatRequest.getGuestName() == null || chatRequest.getGuestName().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guest name is required");
            }
            chat.setGuestName(chatRequest.getGuestName());
            chat.setIsGuest(true);
        }

        // Lưu tin nhắn trò chuyện
        RoomChat savedChat = roomChatRepository.save(chat);

        // Tạo phản hồi
        ChatMessageResponse response = ChatMessageResponse.fromRoomChat(savedChat, localStorageService);

        // Phát tin nhắn đến tất cả người dùng trong phòng thông qua WebSocketService
        webSocketService.sendChatMessage(room.getId(), response);

        return response;
    }
}