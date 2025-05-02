package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}