package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.request.LoginRequest;
import com.huy.quizme_backend.dto.request.RegisterRequest;
import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.AuthResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    // Phương thức đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        // Xác thực và lấy token
        AuthResponse authResponse = authService.login(loginRequest);

        // Chuẩn bị dữ liệu trả về trong phần data
        ApiResponse<AuthResponse> data = ApiResponse.success(authResponse, "Login successful");

        return ResponseEntity.ok(data);
    }

    // Phương thức đăng ký
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        // Tạo mới user dựa trên thông tin đăng ký
        UserResponse registeredUser = authService.register(registerRequest);

        // Tạo phản hồi thành công
        ApiResponse<UserResponse> data = ApiResponse.created(registeredUser, "User registered successfully");

        // Trả về phản hồi
        return ResponseEntity.status(HttpStatus.CREATED).body(data);
    }
}
