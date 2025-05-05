package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.Room;
import com.huy.quizme_backend.enity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCode(String code);
    List<Room> findByHost(User host);
    List<Room> findByStatus(Room.Status status);
}