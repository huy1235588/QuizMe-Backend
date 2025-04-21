package com.huy.quizme_backend.dto.response;

import com.huy.quizme_backend.enity.Role;
import com.huy.quizme_backend.enity.User;
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
    private boolean isActive;

    // Map từ User entity sang UserResponse DTO
    public static UserResponse toUserResponse(User user) {
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

    // Map từ UserResponse DTO sang User entity
    public User toUser() {
        return User.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .fullName(this.fullName)
                .profileImage(this.profileImage)
                .createdAt(this.createdAt != null ? java.time.LocalDateTime.parse(this.createdAt) : null)
                .updatedAt(this.updatedAt != null ? java.time.LocalDateTime.parse(this.updatedAt) : null)
                .lastLogin(this.lastLogin != null ? java.time.LocalDateTime.parse(this.lastLogin) : null)
                .role(this.role)
                .isActive(this.isActive)
                .build();
    }
}
