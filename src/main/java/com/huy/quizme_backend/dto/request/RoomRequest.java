package com.huy.quizme_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
    @NotBlank(message = "Room name is required")
    private String name;

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @Min(value = 2, message = "Room must allow at least 2 players")
    private int maxPlayers = 10;
}