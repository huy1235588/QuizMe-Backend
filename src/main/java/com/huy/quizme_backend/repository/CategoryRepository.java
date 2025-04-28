package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.Category;
import io.micrometer.common.KeyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    // Lấy danh sách các danh mục hoạt động
    List<Category> findAllByIsActive(boolean isActive);
}