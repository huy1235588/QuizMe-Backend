package com.huy.quizme_backend.dto.response;

import com.huy.quizme_backend.enity.Room;
import com.huy.quizme_backend.service.LocalStorageService;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
public class RoomResponse {
    private Long id;
    private String name;
    private String code;
    private QuizResponse quiz;
    private UserResponse host;
    private boolean hasPassword;
    private Boolean isPublic;
    private int currentPlayerCount;
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
                .hasPassword(room.getPassword() != null && !room.getPassword().isEmpty())
                .isPublic(room.getIsPublic())
                .maxPlayers(room.getMaxPlayers())
                .status(room.getStatus().name())
                .startTime(room.getStartTime())
                .endTime(room.getEndTime())
                .createdAt(room.getCreatedAt())
                .participants(room.getParticipants().stream()
                        .map(participant -> ParticipantResponse.fromRoomParticipant(participant, localStorageService))
                        .collect(Collectors.toList()))
                .build();
    }
}