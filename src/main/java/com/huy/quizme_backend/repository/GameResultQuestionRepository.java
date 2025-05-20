package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.GameResult;
import com.huy.quizme_backend.enity.GameResultQuestion;
import com.huy.quizme_backend.enity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameResultQuestionRepository extends JpaRepository<GameResultQuestion, Long> {
    List<GameResultQuestion> findByGameResultId(Long gameResultId);
    List<GameResultQuestion> findByGameResult(GameResult gameResult);
    Optional<GameResultQuestion> findByGameResultAndQuestion(GameResult gameResult, Question question);
}
