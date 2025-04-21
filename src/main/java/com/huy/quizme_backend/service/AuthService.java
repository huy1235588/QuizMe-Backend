package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.LoginRequest;
import com.huy.quizme_backend.dto.request.RegisterRequest;
import com.huy.quizme_backend.dto.response.AuthResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.enity.Role;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.repository.UserRepository;
import com.huy.quizme_backend.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    // Phương thức đăng nhập
    public AuthResponse login(LoginRequest loginRequest) {
        // Xác thực người dùng
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        // Nếu xác thực thành công, set Authentication vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo JWT token
        String jwt = tokenProvider.generateToken(authentication);

        // Lấy ngày hết hạn của token
        Date expiryDate = tokenProvider.getExpirationDateFromJWT(jwt);

        // Lấy User từ
        String usernameOrEmail = loginRequest.getUsernameOrEmail();
        User user = userRepository
                .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Kiểm tra xem người dùng có bị khóa hay không
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "User is not active");
        }

        // Trả về token
        return new AuthResponse(
                jwt,
                expiryDate,
                UserResponse.toUserResponse(user)
        );
    }

    // Phương thức đăng ký
    public UserResponse register(RegisterRequest registerRequest) {
        // Kiểm tra xem người dùng đã tồn tại chưa
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        // Kiểm tra xem email đã tồn tại chưa
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // Kiểm tra xem mật khẩu có khớp với xác nhận không
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        // Tạo người dùng mới
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .role(Role.USER) // Gán vai trò mặc định là USER
                .isActive(true) // Gán trạng thái hoạt động mặc định là true
                .build();

        // Lưu người dùng vào cơ sở dữ liệu
        User saved = userRepository.save(user);

        // Trả về người dùng đã lưu
        return UserResponse.toUserResponse(saved);
    }
}
