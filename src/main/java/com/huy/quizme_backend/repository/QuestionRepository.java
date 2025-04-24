package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // Find questions by quiz ID
    List<Question> findByQuizIdOrderByOrderNumber(Long quizId);

    // Find questions by quiz ID and order number range
    List<Question> findByQuizIdAndOrderNumberBetweenOrderByOrderNumber(Long quizId, Integer startOrder, Integer endOrder);
}