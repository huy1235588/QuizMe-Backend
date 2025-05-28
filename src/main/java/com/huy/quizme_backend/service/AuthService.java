package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.LoginRequest;
import com.huy.quizme_backend.dto.request.RegisterRequest;
import com.huy.quizme_backend.dto.response.AuthResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.enity.RefreshToken;
import com.huy.quizme_backend.enity.UserProfile;
import com.huy.quizme_backend.enity.enums.Role;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.repository.RefreshTokenRepository;
import com.huy.quizme_backend.repository.UserProfileRepository;
import com.huy.quizme_backend.repository.UserRepository;
import com.huy.quizme_backend.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
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
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final LocalStorageService localStorageService;

    // Tạo JWT access token và refresh token
    private AuthTokens createTokens(Authentication authentication) {
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

        return new AuthTokens(accessToken, accessExpiry, refreshToken, refreshExpiry, jti);
    }

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

        // Tạo tokens
        AuthTokens tokens = createTokens(authentication);

        // Lưu refresh token vào cơ sở dữ liệu
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(tokens.getRefreshToken())
                .jti(tokens.getJti())
                .issuedAt(Instant.now())
                .expiresAt(tokens.getRefreshExpiry())
                .revoked(false)
                .build();

        // Xóa refresh token cũ nếu có
        refreshTokenRepository.deleteByUser(user);

        // Lưu refresh token mới vào cơ sở dữ liệu
        refreshTokenRepository.save(refreshTokenEntity);

        // Trả về token với chuyển đổi Cloudinary URL cho ảnh đại diện
        return new AuthResponse(
                tokens.getAccessToken(),
                tokens.getAccessExpiry(),
                tokens.getRefreshToken(),
                tokens.getRefreshExpiry(),
                UserResponse.fromUser(user, localStorageService)
        );
    }

    // Phương thức đăng ký
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
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

        // Lưu người dùng trước
        user = userRepository.save(user);

        // Tạo và lưu hồ sơ người dùng mới
        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .build();
        userProfileRepository.save(userProfile);

        // Tạo authentication object để tạo token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        // Tạo tokens
        AuthTokens tokens = createTokens(authentication);

        // Lưu refresh token vào cơ sở dữ liệu
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(tokens.getRefreshToken())
                .jti(tokens.getJti())
                .issuedAt(Instant.now())
                .expiresAt(tokens.getRefreshExpiry())
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        // Trả về token với chuyển đổi Cloudinary URL cho ảnh đại diện
        return new AuthResponse(
                tokens.getAccessToken(),
                tokens.getAccessExpiry(),
                tokens.getRefreshToken(),
                tokens.getRefreshExpiry(),
                UserResponse.fromUser(user, localStorageService)
        );
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

        // Trả về token mới với chuyển đổi Cloudinary URL
        return new AuthResponse(
                accessToken,
                accessExpiry,
                refreshToken,
                token.getExpiresAt(),
                UserResponse.fromUser(token.getUser(), localStorageService)
        );
    }
}

// Class để chứa thông tin token
class AuthTokens {
    private final String accessToken;
    private final Instant accessExpiry;
    private final String refreshToken;
    private final Instant refreshExpiry;
    private final String jti;

    public AuthTokens(String accessToken, Instant accessExpiry, String refreshToken, Instant refreshExpiry, String jti) {
        this.accessToken = accessToken;
        this.accessExpiry = accessExpiry;
        this.refreshToken = refreshToken;
        this.refreshExpiry = refreshExpiry;
        this.jti = jti;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Instant getAccessExpiry() {
        return accessExpiry;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getRefreshExpiry() {
        return refreshExpiry;
    }

    public String getJti() {
        return jti;
    }
}

