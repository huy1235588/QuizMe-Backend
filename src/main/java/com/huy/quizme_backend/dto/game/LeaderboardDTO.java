package com.huy.quizme_backend.dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO dùng để cập nhật bảng xếp hạng trong quá trình chơi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDTO {
    private List<PlayerRankingDTO> rankings;

    /**
     * DTO cho thông tin xếp hạng của một người chơi
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerRankingDTO {
        private Long userId;
        private String username;
        private Integer score;
        private Integer rank;
        private String avatar;
        private Boolean isGuest;
        private Integer correctCount;
    }
}
