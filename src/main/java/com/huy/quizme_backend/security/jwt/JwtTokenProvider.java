package com.huy.quizme_backend.security.jwt;

import com.huy.quizme_backend.enity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret; // Khóa bí mật để mã hóa và giải mã token

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationInMs; // Thời gian hết hạn của access token (ms)

    @Value("${jwt.refresh-expiration-ms}")
    private long jwtRefreshExpirationInMs; // Thời gian hết hạn của refresh token (ms)

    // Tạo SecretKey từ chuỗi base64 secret
    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Tạo JWT token với subject, thời gian phát hành và thời gian hết hạn
     *
     * @param subject          Tên người dùng
     * @param expirationMillis Thời gian hết hạn (ms)
     * @return JWT token
     */
    private String buildToken(String subject, long expirationMillis) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expirationDate = Date.from(now.plusMillis(expirationMillis));

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // Đặt ID là tên người dùng
                .subject(subject) // Đặt tên người dùng làm subject
                .issuedAt(issuedAt) // Đặt thời gian phát hành
                .expiration(expirationDate) // Đặt thời gian hết hạn
                .signWith(getSecretKey(), Jwts.SIG.HS512) // Ký token bằng secret key
                .compact(); // Tạo token
    }

    // Lấy claims từ JWT token
    private Claims getClaimsFromToken(String token) {
        // Phân tích token và lấy claims
        return Jwts.parser()
                .verifyWith(getSecretKey()) // Đặt secret key để xác thực
                .build()
                .parseSignedClaims(token)
                .getPayload(); // Trả về claims
    }

    // Tạo access token từ Authentication
    public String generateAccessToken(Authentication authentication) {
        // Lấy username từ Authentication
        String username = ((User) authentication.getPrincipal()).getUsername();

        // Tạo token
        return buildToken(username, jwtRefreshExpirationInMs);
    }

    // Tạo access token từ user
    public String generateAccessToken(User user) {
        // Lấy username từ user
        String username = user.getUsername();

        // Tạo token
        return buildToken(username, jwtExpirationInMs);
    }

    // Tạo refresh token từ Authentication
    public String generateRefreshToken(Authentication authentication) {
        // Lấy username từ Authentication
        String username = ((User) authentication.getPrincipal()).getUsername();

        System.out.println("Username: " + username);

        // Tạo token
        return buildToken(username, jwtRefreshExpirationInMs);
    }

    // Lấy username từ JWT token
    public String getUsernameFromJWT(String token) {
        // Trả về username từ claims
        return getClaimsFromToken(token).getSubject();
    }

    // Lấy ngày hết hạn từ JWT token
    public Instant getExpirationDateFromJWT(String token) {
        // Trả về ngày hết hạn từ claims
        return getClaimsFromToken(token)
                .getExpiration()
                .toInstant();
    }

    public String getJtiFromJWT(String token) {
        // Trả về jti từ claims
        return getClaimsFromToken(token).getId();
    }

    // Xác thực JWT token
    public boolean validateToken(String token) {
        try {
            // Kiểm tra tính hợp lệ của token
            Jwts.parser().verifyWith(getSecretKey()) // Xác thực token bằng secret key
                    .build().parseSignedClaims(token); // Phân tích claims từ token

            // Token hợp lệ
            return true;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
        } catch (io.jsonwebtoken.SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());

        } catch (Exception e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        // Token không hợp lệ
        return false;
    }
}
