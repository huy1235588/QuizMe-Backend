package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // Find questions by quiz ID
    List<Question> findByQuizIdOrderByOrderNumber(Long quizId);

    // Find questions by quiz ID and order number range
    List<Question> findByQuizIdAndOrderNumberBetweenOrderByOrderNumber(Long quizId, Integer startOrder, Integer endOrder);

    List<Question> findByQuiz(Quiz quiz);
    
    // Find question by ID with options eagerly loaded to avoid lazy loading issues in timer threads
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.id = :questionId")
    Optional<Question> findByIdWithOptions(@Param("questionId") Long questionId);
}