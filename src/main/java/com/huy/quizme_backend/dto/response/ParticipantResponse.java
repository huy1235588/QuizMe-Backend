package com.huy.quizme_backend.dto.response;

import com.huy.quizme_backend.enity.RoomParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantResponse {
    private Long id;
    private UserResponse user;
    private int score;
    private boolean isHost;
    private LocalDateTime joinedAt;
    private boolean isGuest;
    private String guestName;

    public static ParticipantResponse fromRoomParticipant(RoomParticipant participant) {
        return ParticipantResponse.builder()
                .id(participant.getId())
                .user(participant.getUser() != null ?
                        UserResponse.fromUser(participant.getUser(), null) : null)
                .score(participant.getScore())
                .isHost(participant.isHost())
                .joinedAt(participant.getJoinedAt())
                .isGuest(participant.isGuest())
                .guestName(participant.getGuestName())
                .build();
    }
}