package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.GamePlayerAnswer;
import com.huy.quizme_backend.enity.GameResult;
import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GamePlayerAnswerRepository extends JpaRepository<GamePlayerAnswer, Long> {
    // Tìm theo GameResult
    List<GamePlayerAnswer> findByGameResultId(Long gameResultId);

    List<GamePlayerAnswer> findByGameResultAndParticipantId(GameResult gameResult, Long participantId);

    List<GamePlayerAnswer> findByGameResultAndParticipant(GameResult gameResult, RoomParticipant participant);

    // Tìm theo Question
    List<GamePlayerAnswer> findByQuestionId(Long questionId);

    List<GamePlayerAnswer> findByQuestion(Question question);

    // Tìm theo Participant
    List<GamePlayerAnswer> findByRoomParticipantId(Long participantId);

    // Tìm cụ thể
    Optional<GamePlayerAnswer> findByRoomParticipantIdAndQuestionId(Long participantId, Long questionId);

    // Đếm đáp án đúng
    @Query("SELECT COUNT(gpa) FROM GamePlayerAnswer gpa WHERE gpa.gameResult.id = ?1 AND gpa.isCorrect = true")
    Long countCorrectAnswersByGameResult(Long gameResultId);

    @Query("SELECT COUNT(gpa) FROM GamePlayerAnswer gpa WHERE gpa.gameResult.id = ?1 AND gpa.question.id = ?2 AND gpa.isCorrect = true")
    Long countCorrectAnswersByGameResultAndQuestionId(Long gameResultId, Long questionId);

    // Đếm số câu trả lời đúng của một người chơi
    @Query("SELECT COUNT(gpa) FROM GamePlayerAnswer gpa WHERE gpa.participant.id = :participantId AND gpa.isCorrect = true")
    Long countCorrectAnswersByParticipant(@Param("participantId") Long participantId);

    // Tính điểm và thời gian trung bình
    @Query("SELECT AVG(gpa.score) FROM GamePlayerAnswer gpa WHERE gpa.question.id = :questionId")
    Double getAverageScoreByQuestion(@Param("questionId") Long questionId);

    @Query("SELECT AVG(gpa.answerTime) FROM GamePlayerAnswer gpa WHERE gpa.question.id = :questionId")
    Double getAverageAnswerTimeByQuestion(@Param("questionId") Long questionId);
}
