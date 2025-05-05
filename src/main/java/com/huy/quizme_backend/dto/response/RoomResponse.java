package com.huy.quizme_backend.dto.response;

import com.huy.quizme_backend.enity.Room;
import com.huy.quizme_backend.service.LocalStorageService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
    private Long id;
    private String name;
    private String code;
    private QuizResponse quiz;
    private UserResponse host;
    private int maxPlayers;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private List<ParticipantResponse> participants;

    public static RoomResponse fromRoom(Room room, LocalStorageService localStorageService) {
        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .code(room.getCode())
                .quiz(QuizResponse.fromQuiz(room.getQuiz(), localStorageService))
                .host(UserResponse.fromUser(room.getHost(), localStorageService))
                .maxPlayers(room.getMaxPlayers())
                .status(room.getStatus().name())
                .startTime(room.getStartTime())
                .endTime(room.getEndTime())
                .createdAt(room.getCreatedAt())
                .participants(room.getParticipants().stream()
                        .map(ParticipantResponse::fromRoomParticipant)
                        .collect(Collectors.toList()))
                .build();
    }
}