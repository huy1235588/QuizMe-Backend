package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.GameResult;
import com.huy.quizme_backend.enity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, Long> {
    List<GameResult> findByRoomId(Long roomId);
    Optional<GameResult> findTopByRoomOrderByStartTimeDesc(Room room);
    List<GameResult> findByQuizId(Long quizId);
}
