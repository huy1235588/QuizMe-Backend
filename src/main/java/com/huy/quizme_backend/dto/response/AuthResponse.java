package com.huy.quizme_backend.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String AccessToken;
    private Date ExpiresAt;
    private UserResponse user;

    public AuthResponse(String accessToken) {
        AccessToken = accessToken;
    }
}
