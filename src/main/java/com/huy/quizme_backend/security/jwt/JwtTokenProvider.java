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
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret; // Khóa bí mật để mã hóa và giải mã token

    @Value("${jwt.expiration-ms}")
    private int jwtExpirationInMs; // Thời gian hết hạn của token (ms)

    // Tạo SecretKey từ chuỗi base64 secret
    public SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Tạo token JWT từ thông tin Authentication
    public String generateToken(Authentication authentication) {
        // Lấy user từ principal
        User userPrincipal = (User) authentication.getPrincipal();

        // Tạo ngày hết hạn cho token
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // Tạo token
        return io.jsonwebtoken.Jwts.builder()
                .subject(userPrincipal.getUsername()) // Đặt tên người dùng làm subject
                .issuedAt(now) // Đặt thời gian phát hành
                .expiration(expiryDate) // Đặt thời gian hết hạn
                .signWith(getSecretKey(), Jwts.SIG.HS512) // Ký token bằng secret key
                .compact(); // Tạo token
    }

    // Tạo token JWT từ User entity
    public String generateToken(User user) {
        // Tạo ngày hết hạn cho token
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // Tạo token
        return io.jsonwebtoken.Jwts.builder()
                .subject(user.getUsername()) // Đặt tên người dùng làm subject
                .issuedAt(now) // Đặt thời gian phát hành
                .expiration(expiryDate) // Đặt thời gian hết hạn
                .signWith(getSecretKey(), Jwts.SIG.HS512) // Ký token bằng secret key
                .compact(); // Tạo token
    }

    // Lấy username từ JWT token
    public String getUsernameFromJWT(String token) {
        // Lấy claims từ token
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey()) // Xác thực token bằng secret key
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Trả về username từ claims
        return claims.getSubject();
    }

    // Lấy ngày hết hạn từ JWT token
    public Date getExpirationDateFromJWT(String token) {
        // Lấy claims từ token
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey()) // Xác thực token bằng secret key
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Trả về ngày hết hạn từ claims
        return claims.getExpiration();
    }

    // Xác thực JWT token
    public boolean validateToken(String token) {
        try {
            // Kiểm tra tính hợp lệ của token
            Jwts.parser()
                    .verifyWith(getSecretKey()) // Xác thực token bằng secret key
                    .build()
                    .parseSignedClaims(token); // Phân tích claims từ token

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
