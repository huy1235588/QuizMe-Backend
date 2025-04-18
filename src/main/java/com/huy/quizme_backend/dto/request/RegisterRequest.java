package com.huy.quizme_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 2, max = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 2, max = 100)
    private String password;

    @NotBlank
    @Size(min = 2, max = 100)
    private String confirmPassword;

    @NotBlank
    @Size(max = 100)
    private String fullName;
}
