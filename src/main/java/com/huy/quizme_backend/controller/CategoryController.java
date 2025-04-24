package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.CategoryResponse;
import com.huy.quizme_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    
    /**
     * API lấy danh sách tất cả các danh mục
     * @return Danh sách các CategoryResponse
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ApiResponse.success(categories, "Categories retrieved successfully");
    }
    
    /**
     * API lấy danh mục theo ID
     * @param id ID của danh mục
     * @return CategoryResponse
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ApiResponse.success(category, "Category retrieved successfully");
    }
}