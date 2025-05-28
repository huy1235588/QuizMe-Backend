package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.UserProfileResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * API lấy thông tin người dùng theo ID
     *
     * @param id ID của người dùng
     * @return Thông tin người dùng
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ApiResponse.success(user, "User retrieved successfully");
    }

    /**
     * API lấy danh sách người dùng có tổng số quiz được chơi nhiều nhất
     *
     * @return Danh sách người dùng
     */
    @GetMapping("/top")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<UserResponse>> getTopUsersByTotalQuizPlays() {
        List<UserResponse> topUsers = userService.getTopUsersByTotalQuizPlays();
        return ApiResponse.success(topUsers, "Top users retrieved successfully");
    }

    /**
     * API lấy tổng số người dùng trong hệ thống
     *
     * @return Tổng số người dùng
     */
    @GetMapping("/count")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Map<String, Long>> getUserCount() {
        long count = userService.countUsers();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ApiResponse.success(response, "User count retrieved successfully");
    }

    /**
     * API lấy thông tin profile của người dùng hiện tại đã đăng nhập
     *
     * @param principal Thông tin người dùng đăng nhập hiện tại
     * @return Thông tin profile người dùng
     */
    @GetMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserProfileResponse> getCurrentUserProfile(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User currentUser = (User) ((Authentication) principal).getPrincipal();
        UserProfileResponse profile = userService.getCurrentUserProfile(currentUser);

        return ApiResponse.success(profile, "User profile retrieved successfully");
    }

    /**
     * API lấy thông tin profile của người dùng theo ID
     *
     * @param id ID của người dùng
     * @return Thông tin profile người dùng
     */
    @GetMapping("/profile/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserProfileResponse> getUserProfileById(@PathVariable Long id) {
        UserProfileResponse profile = userService.getUserProfile(id);
        return ApiResponse.success(profile, "User profile retrieved successfully");
    }

    /**
     * API upload avatar cho người dùng hiện tại
     *
     * @param avatarFile File ảnh avatar
     * @param principal  Thông tin người dùng đăng nhập hiện tại
     * @return Thông tin người dùng đã cập nhật
     */
    @PostMapping("/avatar/upload")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> uploadAvatar(
            @RequestParam("avatarFile") MultipartFile avatarFile,
            Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User currentUser = (User) ((Authentication) principal).getPrincipal();
        UserResponse updatedUser = userService.uploadAvatar(currentUser, avatarFile);

        return ApiResponse.success(updatedUser, "Avatar uploaded successfully");
    }

    /**
     * API xóa avatar của người dùng hiện tại
     *
     * @param principal Thông tin người dùng đăng nhập hiện tại
     * @return Thông tin người dùng đã cập nhật
     */
    @DeleteMapping("/avatar")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> removeAvatar(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User currentUser = (User) ((Authentication) principal).getPrincipal();
        UserResponse updatedUser = userService.removeAvatar(currentUser);

        return ApiResponse.success(updatedUser, "Avatar removed successfully");
    }
}