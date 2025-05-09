package com.huy.quizme_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotBlank(message = "Message cannot be empty")
    private String message;
    
    private String guestName;
}