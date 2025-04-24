package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.response.CategoryResponse;
import com.huy.quizme_backend.enity.Category;
import com.huy.quizme_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    
    /**
     * Lấy danh sách tất cả các danh mục
     * @return Danh sách các CategoryResponse
     */
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromCategory)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh mục theo ID
     * @param id ID của danh mục
     * @return CategoryResponse
     */
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return CategoryResponse.fromCategory(category);
    }
}