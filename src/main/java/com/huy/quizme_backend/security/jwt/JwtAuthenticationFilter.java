package com.huy.quizme_backend.security.jwt;

import com.huy.quizme_backend.security.UserDetailsServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException,
            IOException {
        try {
            // Lấy token từ request
            String token = resolveToken(request);

            // Kiểm tra xem JWT có hợp lệ không
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                // Lấy tên người dùng từ token
                String username = jwtTokenProvider.getUsernameFromJWT(token);

                // Tạo đối tượng Authentication từ tên người dùng
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Tạo đối tượng Authentication và lưu vào SecurityContext
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Thiết lập chi tiết xác thực
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Lưu đối tượng Authentication vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | IllegalArgumentException ex) {
            logger.error("Failed to authenticate JWT: {}", ex.getMessage());
        }
        // Tiếp tục chuỗi bộ lọc
        filterChain.doFilter(request, response);
    }

    // Lấy JWT từ request
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Bỏ qua "Bearer " để lấy token
        }
        return null;
    }
}
