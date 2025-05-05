package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.Room;
import com.huy.quizme_backend.enity.RoomParticipant;
import com.huy.quizme_backend.enity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    List<RoomParticipant> findByRoom(Room room);
    Optional<RoomParticipant> findByRoomAndUser(Room room, User user);
    Optional<RoomParticipant> findByRoomAndGuestName(Room room, String guestName);
    int countByRoom(Room room);
}