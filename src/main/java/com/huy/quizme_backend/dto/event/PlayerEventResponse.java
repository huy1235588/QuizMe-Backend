package com.huy.quizme_backend.dto.event;

import com.huy.quizme_backend.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerEventResponse {
    private Long userId;
    private String username;
    private String profileImage;
    private boolean isGuest;
    private String guestName;
    private String message;
    private String eventType; // "join" or "leave"
    
    public static PlayerEventResponse fromUser(UserResponse user, String message, String eventType) {
        return PlayerEventResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .profileImage(user.getProfileImage())
                .isGuest(false)
                .message(message)
                .eventType(eventType)
                .build();
    }
    
    public static PlayerEventResponse fromGuest(String guestName, String message, String eventType) {
        return PlayerEventResponse.builder()
                .isGuest(true)
                .guestName(guestName)
                .message(message)
                .eventType(eventType)
                .build();
    }
}