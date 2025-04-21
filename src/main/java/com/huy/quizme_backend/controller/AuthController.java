package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.request.LoginRequest;
import com.huy.quizme_backend.dto.request.RegisterRequest;
import com.huy.quizme_backend.dto.request.TokenRequest;
import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.AuthResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    // Phương thức đăng nhập
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        // Xác thực và lấy token
        AuthResponse authResponse = authService.login(loginRequest);

        return ApiResponse.success(
                authResponse,
                "Login successful"
        );
    }

    // Phương thức đăng ký
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        // Tạo mới user dựa trên thông tin đăng ký
        UserResponse registeredUser = authService.register(registerRequest);

        // Trả về phản hồi
        return ApiResponse.created(
                registeredUser,
                "User registered successfully"
        );
    }

    // Phương thức đăng xuất
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> logout(
            @Valid @RequestBody TokenRequest tokenRequest
    ) {
        // Đăng xuất người dùng
        authService.logout(tokenRequest.getRefreshToken());

        // Trả về phản hồi
        return ApiResponse.success(
                null,
                "Logout successful"
        );
    }

    // Phương thức làm mới token
    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AuthResponse> refreshToken(
            @Valid @RequestBody TokenRequest tokenRequest
    ) {
        // Làm mới token
        AuthResponse authResponse = authService.refreshToken(tokenRequest.getRefreshToken());

        return ApiResponse.success(
                authResponse,
                "Token refreshed successfully"
        );
    }
}
