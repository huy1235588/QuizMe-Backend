package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.Room;
import com.huy.quizme_backend.enity.RoomParticipant;
import com.huy.quizme_backend.enity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    // Tìm kiếm theo phòng
    List<RoomParticipant> findByRoom(Room room);

    List<RoomParticipant> findByRoomId(Long roomId);

    List<RoomParticipant> findByRoomIdOrderByScoreDesc(Long roomId);

    // Tìm người tham gia cụ thể
    Optional<RoomParticipant> findByRoomAndUser(Room room, User user);

    Optional<RoomParticipant> findByRoomIdAndUserId(Long roomId, Long userId);

    Optional<RoomParticipant> findByRoomAndGuestName(Room room, String guestName);

    Optional<RoomParticipant> findByRoomIdAndGuestId(Long roomId, String guestId);

    // Đếm số lượng người tham gia
    int countByRoom(Room room);

    int countByRoomId(Long roomId);

    int countByRoomIdAndIsReady(Long roomId, boolean isReady);

    // Tìm ID của người tham gia dựa trên roomId và userId
    @Query("SELECT p.id FROM RoomParticipant p WHERE p.room.id = :roomId AND p.user.id = :userId")
    Optional<Long> findParticipantIdByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);
}