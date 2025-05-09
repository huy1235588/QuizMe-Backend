package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.Room;
import com.huy.quizme_backend.enity.RoomChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomChatRepository extends JpaRepository<RoomChat, Long> {
    List<RoomChat> findByRoomOrderBySentAtAsc(Room room);
    List<RoomChat> findByRoomIdOrderBySentAtAsc(Long roomId);
    List<RoomChat> findTop50ByRoomIdOrderBySentAtDesc(Long roomId);
}