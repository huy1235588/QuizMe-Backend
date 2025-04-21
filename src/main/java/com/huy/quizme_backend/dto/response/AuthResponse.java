package com.huy.quizme_backend.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String AccessToken;
    private Instant AccessTokenExpiry;
    private String RefreshToken;
    private Instant RefreshTokenExpiry;
    private UserResponse user;

    public AuthResponse(String accessToken) {
        AccessToken = accessToken;
    }
}
