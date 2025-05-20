package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.GamePlayerAnswer;
import com.huy.quizme_backend.enity.GameResult;
import com.huy.quizme_backend.enity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamePlayerAnswerRepository extends JpaRepository<GamePlayerAnswer, Long> {
    List<GamePlayerAnswer> findByGameResultId(Long gameResultId);
    List<GamePlayerAnswer> findByGameResultAndParticipantId(GameResult gameResult, Long participantId);
    List<GamePlayerAnswer> findByGameResultAndParticipant(GameResult gameResult, RoomParticipant participant);
    List<GamePlayerAnswer> findByQuestionId(Long questionId);
    
    @Query("SELECT COUNT(gpa) FROM GamePlayerAnswer gpa WHERE gpa.gameResult.id = ?1 AND gpa.isCorrect = true")
    Long countCorrectAnswersByGameResult(Long gameResultId);
    
    @Query("SELECT COUNT(gpa) FROM GamePlayerAnswer gpa WHERE gpa.gameResult.id = ?1 AND gpa.question.id = ?2 AND gpa.isCorrect = true")
    Long countCorrectAnswersByGameResultAndQuestionId(Long gameResultId, Long questionId);
}
