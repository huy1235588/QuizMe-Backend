package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.request.LoginRequest;
import com.huy.quizme_backend.dto.request.RegisterRequest;
import com.huy.quizme_backend.dto.response.AuthResponse;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
        System.out.println(loginRequest.getUsernameOrEmail());
        // Xác thực người dùng
        AuthResponse authResponse = authService.login(loginRequest);

        // Trả về token
        return ResponseEntity.ok(authResponse);
    }

    // Phương thức đăng ký
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        // Tạo người dùng mới
        User registeredUser = authService.register(registerRequest);

        // Tạo URI cho user mới được tạo (tùy chọn)
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(registeredUser.getUsername()).toUri();

        // Trả về 201 Created và thông tin user (không bao gồm password) hoặc chỉ thông báo thành công
        // Tạm thời trả về thông báo đơn giản
        return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký tài khoản thành công!");
        // Hoặc return ResponseEntity.created(location).body("User registered successfully!");
    }
}
