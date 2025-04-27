package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.Difficulty;
import com.huy.quizme_backend.enity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByIsPublicTrue();
    List<Quiz> findByDifficulty(Difficulty difficulty);
    
    // Phương thức tìm kiếm quiz với phân trang và lọc
    @Query("SELECT q FROM Quiz q " +
           "WHERE (:categoryId IS NULL OR q.category.id = :categoryId) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:isPublic IS NULL OR q.isPublic = :isPublic) " +
           "AND (:search IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(q.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Quiz> findQuizzesWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("difficulty") Difficulty difficulty,
            @Param("isPublic") Boolean isPublic,
            @Param("search") String search,
            Pageable pageable);

    // Phương thức tìm kiếm quiz theo tab
    @Query("SELECT q FROM Quiz q " +
           "WHERE (:tab = 'newest' OR :tab IS NULL) " +
           "ORDER BY q.createdAt DESC")
    Page<Quiz> findNewestQuizzes(
            @Param("tab") String tab,
            Pageable pageable);

    @Query("SELECT q FROM Quiz q " +
           "WHERE :tab = 'popular' " +
           "ORDER BY q.playCount DESC")
    Page<Quiz> findPopularQuizzes(
            @Param("tab") String tab,
            Pageable pageable);
}