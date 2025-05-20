package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.GamePlayerAnswer;
import com.huy.quizme_backend.enity.GamePlayerAnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamePlayerAnswerOptionRepository extends JpaRepository<GamePlayerAnswerOption, Long> {
    List<GamePlayerAnswerOption> findByGamePlayerAnswerId(Long gamePlayerAnswerId);
    List<GamePlayerAnswerOption> findByGamePlayerAnswer(GamePlayerAnswer gamePlayerAnswer);
    void deleteByGamePlayerAnswerId(Long gamePlayerAnswerId);
}
