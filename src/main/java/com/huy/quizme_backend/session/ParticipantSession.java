package com.huy.quizme_backend.session;

import com.huy.quizme_backend.enity.GamePlayerAnswer;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mô hình phiên của người chơi trong game.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantSession {
    private Long userId;
    private String username;
    private Integer score;
    private Integer rank;
    private ConcurrentMap<Long, GamePlayerAnswer> answers = new ConcurrentHashMap<>();
    private ConnectionStatus connectionStatus;
    private LocalDateTime joinedAt;
    private LocalDateTime disconnectedAt;
    private Set<String> sessionIds = ConcurrentHashMap.newKeySet();
}
