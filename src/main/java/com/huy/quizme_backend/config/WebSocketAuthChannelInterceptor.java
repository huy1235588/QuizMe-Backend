package com.huy.quizme_backend.config;

import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Bộ chặn kênh WebSocket để xác thực người dùng thông qua JWT token
 * khi kết nối WebSocket được thiết lập
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Xử lý tin nhắn trước khi gửi
     * Kiểm tra xác thực khi có lệnh CONNECT từ client
     * @param message Tin nhắn đang được xử lý
     * @param channel Kênh tin nhắn
     * @return Tin nhắn đã được xử lý
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Trích xuất JWT token từ header
            String token = extractTokenFromHeader(accessor);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromJWT(token);
                User userDetails = (User) userDetailsService.loadUserByUsername(username);

                // Tạo đối tượng xác thực
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // Thiết lập xác thực trong accessor
                accessor.setUser(authentication);
                log.info("User authenticated for WebSocket connection: {}", username);
            }
        }

        return message;
    }

    /**
     * Trích xuất JWT token từ header của request
     * @param accessor StompHeaderAccessor để truy cập các header
     * @return Token JWT hoặc null nếu không tìm thấy
     */
    private String extractTokenFromHeader(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}