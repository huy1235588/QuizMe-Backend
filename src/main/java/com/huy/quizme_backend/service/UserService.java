package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.response.UserProfileResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import com.huy.quizme_backend.util.FileValidationUtil;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final LocalStorageService localStorageService;

    /**
     * Lấy thông tin người dùng theo ID
     *
     * @param id ID của người dùng
     * @return Thông tin người dùng
     */
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> UserResponse.fromUser(user, localStorageService))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }

    /**
     * Lấy Top người dùng có tổng số quiz được chơi nhiều nhất
     *
     * @return Danh sách người dùng
     */
    public List<UserResponse> getTopUsersByTotalQuizPlays() {
        return userRepository.findTopUsersByTotalQuizPlays()
                .stream()
                .map(user -> UserResponse.fromUser(user, localStorageService))
                .collect(Collectors.toList());
    }

    /**
     * Đếm tổng số người dùng trong hệ thống
     *
     * @return Tổng số người dùng
     */
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * Lấy thông tin profile của người dùng theo ID
     *
     * @param userId ID của người dùng
     * @return Thông tin profile của người dùng
     */
    public UserProfileResponse getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    if (user.getUserProfile() == null) {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User profile not found for user with id: " + userId);
                    }
                    return UserProfileResponse.fromUserProfile(user.getUserProfile(), localStorageService);
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + userId));
    }

    /**
     * Lấy thông tin profile của người dùng hiện tại
     *
     * @param user Thông tin người dùng hiện tại đã đăng nhập
     * @return Thông tin profile của người dùng
     */
    public UserProfileResponse getCurrentUserProfile(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        if (user.getUserProfile() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User profile not found for current user");
        }

        return UserProfileResponse.fromUserProfile(user.getUserProfile(), localStorageService);
    }

    /**
     * Upload avatar cho người dùng hiện tại
     *
     * @param user       Thông tin người dùng hiện tại đã đăng nhập
     * @param avatarFile File ảnh avatar cần upload
     * @return Thông tin người dùng đã cập nhật
     */
    @Transactional
    public UserResponse uploadAvatar(User user, MultipartFile avatarFile) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is required");
        }

        // Validate file using utility
        String validationError = FileValidationUtil.getImageValidationError(avatarFile);
        if (validationError != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationError);
        }

        try {
            // Lưu avatar cũ để xóa sau khi upload thành công
            String oldAvatarUrl = user.getProfileImage();

            // Upload ảnh mới
            String newAvatarFilename = localStorageService.uploadProfileImage(avatarFile, user.getId());

            if (newAvatarFilename == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload avatar");
            }

            // Cập nhật thông tin người dùng
            user.setProfileImage(newAvatarFilename);
            User savedUser = userRepository.save(user);

            // Xóa avatar cũ nếu có
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                localStorageService.deleteProfileImage(oldAvatarUrl);
            }

            return UserResponse.fromUser(savedUser, localStorageService);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload avatar: " + e.getMessage());
        }
    }

    /**
     * Xóa avatar của người dùng hiện tại
     *
     * @param user Thông tin người dùng hiện tại đã đăng nhập
     * @return Thông tin người dùng đã cập nhật
     */
    @Transactional
    public UserResponse removeAvatar(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no avatar to remove");
        }

        try {
            // Lưu URL avatar cũ để xóa
            String oldAvatarUrl = user.getProfileImage();

            // Cập nhật thông tin người dùng
            user.setProfileImage(null);
            User savedUser = userRepository.save(user);

            // Xóa file avatar cũ
            localStorageService.deleteProfileImage(oldAvatarUrl);

            return UserResponse.fromUser(savedUser, localStorageService);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to remove avatar: " + e.getMessage());
        }
    }
}
