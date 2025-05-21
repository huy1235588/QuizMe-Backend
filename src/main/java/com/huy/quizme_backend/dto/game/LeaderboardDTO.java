package com.huy.quizme_backend.dto.game;

import com.huy.quizme_backend.enity.RoomParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        // Chuyển đổi từ entity sang DTO
        public static PlayerRankingDTO fromEntity(
                RoomParticipant participant,
                Integer rank,
                Integer correctCount
        ) {
            return PlayerRankingDTO.builder()
                    .userId(participant.getUser() != null ? participant.getUser().getId() : null)
                    .username(participant.getUser() != null ? participant.getUser().getUsername() : participant.getGuestName())
                    .score(participant.getScore())
                    .rank(rank)
                    .avatar(participant.getUser() != null ? participant.getUser().getProfileImage() : null)
                    .isGuest(participant.isGuest())
                    .correctCount(correctCount)
                    .build();
        }
    }

    // Chuyển đổi từ danh sách RoomParticipant entity sang LeaderboardDTO
    public static LeaderboardDTO fromEntityList(
            List<RoomParticipant> participants,
            List<Integer> correctCounts
    ) {
        // Sắp xếp theo điểm số giảm dần
        List<RoomParticipant> sortedParticipants = participants.stream()
                .sorted(Comparator.comparing(RoomParticipant::getScore).reversed())
                .toList();

        // Tạo danh sách xếp hạng
        List<PlayerRankingDTO> rankings = sortedParticipants.stream()
                .map(participant -> {
                    int index = participants.indexOf(participant);
                    int rank = sortedParticipants.indexOf(participant) + 1; // Tính rank
                    Integer correctCount = (index < correctCounts.size()) ? correctCounts.get(index) : 0;
                    return PlayerRankingDTO.fromEntity(participant, rank, correctCount);
                })
                .collect(Collectors.toList());

        return LeaderboardDTO.builder().rankings(rankings).build();
    }
}
