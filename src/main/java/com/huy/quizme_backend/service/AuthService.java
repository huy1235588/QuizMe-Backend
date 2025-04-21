package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.LoginRequest;
import com.huy.quizme_backend.dto.request.RegisterRequest;
import com.huy.quizme_backend.dto.response.AuthResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.enity.RefreshToken;
import com.huy.quizme_backend.enity.Role;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.repository.RefreshTokenRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    // Phương thức đăng nhập
    @Transactional
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

        // Lấy thông tin người dùng từ Authentication
        User user = (User) authentication.getPrincipal();

        // Tạo JWT access token
        String accessToken = tokenProvider.generateAccessToken(authentication);
        // Lấy ngày hết hạn của access token
        Instant accessExpiry = tokenProvider.getExpirationDateFromJWT(accessToken);
        // Tạo JWT refresh token
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        // Lấy ngày hết hạn của refresh token
        Instant refreshExpiry = tokenProvider.getExpirationDateFromJWT(refreshToken);

        // Lấy JTI từ refresh token
        String jti = tokenProvider.getJtiFromJWT(refreshToken);

        // Lưu refresh token vào cơ sở dữ liệu
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .jti(jti)
                .issuedAt(Instant.now())
                .expiresAt(refreshExpiry)
                .revoked(false)
                .build();

        // Xóa refresh token cũ nếu có
        refreshTokenRepository.deleteByUser(user);

        // Lưu refresh token mới vào cơ sở dữ liệu
        refreshTokenRepository.save(refreshTokenEntity);

        // Trả về token
        return new AuthResponse(
                accessToken,
                accessExpiry,
                refreshToken,
                refreshExpiry,
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

        // Trả về người dùng đã lưu vào cơ sở dữ liệu
        return UserResponse.toUserResponse(userRepository.save(user));
    }

    // Phương thức đăng xuất
    @Transactional
    public void logout(String refreshToken) {
        if (!refreshTokenRepository.existsByToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid refresh token");
        }
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    // Phương thức làm mới token
    public AuthResponse refreshToken(String refreshToken) {
        // Kiểm tra tính hợp lệ của refresh token
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        // Kiểm tra xem refresh token có hết hạn không
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        // Tạo mới access token
        String accessToken = tokenProvider.generateAccessToken(token.getUser());
        // Lấy ngày hết hạn của access token
        Instant accessExpiry = tokenProvider.getExpirationDateFromJWT(accessToken);

        // Trả về token mới
        return new AuthResponse(
                accessToken,
                accessExpiry,
                refreshToken,
                token.getExpiresAt(),
                UserResponse.toUserResponse(token.getUser())
        );
    }
}
