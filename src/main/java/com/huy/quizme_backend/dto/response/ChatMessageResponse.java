package com.huy.quizme_backend.dto.response;

import com.huy.quizme_backend.enity.RoomChat;
import com.huy.quizme_backend.service.LocalStorageService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long roomId;
    private UserResponse user;
    private Boolean isGuest;
    private String guestName;
    private String message;
    private String sentAt;

    public static ChatMessageResponse fromRoomChat(RoomChat roomChat, LocalStorageService localStorageService) {
        // Định dạng thời gian
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        String sentAt = roomChat.getSentAt()
                .toLocalTime()
                .format(formatter);


        return ChatMessageResponse.builder()
                .id(roomChat.getId())
                .roomId(roomChat.getRoom().getId())
                .user(roomChat.getUser() != null ?
                        UserResponse.fromUser(roomChat.getUser(), localStorageService) : null)
                .isGuest(roomChat.getIsGuest())
                .guestName(roomChat.getGuestName())
                .message(roomChat.getMessage())
                .sentAt(sentAt)
                .build();
    }
}