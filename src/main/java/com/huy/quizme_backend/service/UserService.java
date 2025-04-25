package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Lấy thông tin người dùng theo ID
     *
     * @param id ID của người dùng
     * @return Thông tin người dùng
     */
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> UserResponse.fromUser(user, cloudinaryService))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + id));
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
     * Tạo tên file ảnh đại diện theo định dạng quy định
     * @param profileId ID của profile người dùng
     * @return Tên file theo quy tắc
     */
    public String generateProfileImageFilename(Long profileId) {
        return cloudinaryService.generateProfileImageFilename(profileId);
    }
}
