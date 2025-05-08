package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.response.UserProfileResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
}
