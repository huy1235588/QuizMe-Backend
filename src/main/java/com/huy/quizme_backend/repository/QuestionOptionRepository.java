package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    // Find options by question ID
    List<QuestionOption> findByQuestionId(Long questionId);

    // Find options by multiple question IDs
    List<QuestionOption> findByQuestionIdIn(List<Long> questionIds);

    // Find correct options for a question
    List<QuestionOption> findByQuestionIdAndIsCorrect(Long questionId, Boolean isCorrect);

    void deleteByQuestionId(Long questionId);
}