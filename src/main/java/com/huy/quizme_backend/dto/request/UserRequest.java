package com.huy.quizme_backend.dto.request;

import com.huy.quizme_backend.enity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserRequest {
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
    @Size(max = 100)
    private String fullName;

    private MultipartFile profileImage;

    @NotBlank
    @Size(max = 20)
    private Role role = Role.USER;

    @NotBlank
    private boolean isActive = true;
}
