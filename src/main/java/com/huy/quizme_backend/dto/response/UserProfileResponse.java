package com.huy.quizme_backend.dto.response;

import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.enity.UserProfile;

import com.huy.quizme_backend.service.LocalStorageService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private Long userId;
    private String username; // Bao gồm một số thông tin người dùng cơ bản
    private String fullName;
    private String profileImage;
    private LocalDate dateOfBirth;
    private String city;
    private String phoneNumber;
    private Integer totalScore;
    private Integer quizzesPlayed;
    private Integer quizzesCreated;
    private Integer totalQuizPlays;

    public static UserProfileResponse fromUserProfile(UserProfile userProfile, LocalStorageService localStorageService) {
        User user = userProfile.getUser();
        String imageUrl = user.getProfileImage() != null && !user.getProfileImage().isEmpty()
                ? localStorageService.getProfileImageUrl(user.getProfileImage())
                : null;

        return UserProfileResponse.builder()
                .id(userProfile.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .profileImage(imageUrl)
                .dateOfBirth(userProfile.getDateOfBirth())
                .city(userProfile.getCity())
                .phoneNumber(userProfile.getPhoneNumber())
                .totalScore(userProfile.getTotalScore())
                .quizzesPlayed(userProfile.getQuizzesPlayed())
                .quizzesCreated(userProfile.getQuizzesCreated())
                .totalQuizPlays(userProfile.getTotalQuizPlays())
                .build();
    }
}