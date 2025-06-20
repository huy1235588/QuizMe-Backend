package com.huy.quizme_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huy.quizme_backend.enity.enums.Role;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.service.LocalStorageService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String profileImage;
    private String createdAt;
    private String updatedAt;
    private String lastLogin;
    private Role role;
    @JsonProperty("isActive")
    private boolean isActive;

    // Map từ User entity sang UserResponse DTO
    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt().toString())
                .updatedAt(user.getUpdatedAt().toString())
                .lastLogin(user.getLastLogin() != null ? user.getLastLogin().toString() : null)
                .role(user.getRole())
                .isActive(user.isActive())
                .build();
    }
    
    // Map từ User entity sang UserResponse DTO với Cloudinary URL
    public static UserResponse fromUser(User user, LocalStorageService localStorageService) {
        UserResponse response = fromUser(user);
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            response.setProfileImage(localStorageService.getProfileImageUrl(user.getProfileImage()));
        }
        return response;
    }

    // Chuyển đổi từ UserResponse DTO sang User entity
    public static User toUser(UserResponse userResponse) {
        return User.builder()
                .id(userResponse.getId())
                .username(userResponse.getUsername())
                .email(userResponse.getEmail())
                .fullName(userResponse.getFullName())
                .profileImage(userResponse.getProfileImage())
                .createdAt(java.time.LocalDateTime.parse(userResponse.getCreatedAt()))
                .updatedAt(java.time.LocalDateTime.parse(userResponse.getUpdatedAt()))
                .lastLogin(userResponse.getLastLogin() != null ? java.time.LocalDateTime.parse(userResponse.getLastLogin()) : null)
                .role(userResponse.getRole())
                .isActive(userResponse.isActive())
                .build();
    }
}
