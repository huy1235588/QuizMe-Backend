package com.huy.quizme_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRequest {
    @NotBlank
    private String refreshToken;
}
