package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.request.CategoryRequest;
import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.CategoryResponse;
import com.huy.quizme_backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    
    /**
     * API tạo mới danh mục (chỉ dành cho ADMIN)
     * @param categoryRequest thông tin danh mục cần tạo
     * @return CategoryResponse của danh mục đã tạo
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse createdCategory = categoryService.createCategory(categoryRequest);
        return ApiResponse.created(createdCategory, "Category created successfully");
    }
    
    /**
     * API cập nhật thông tin danh mục (chỉ dành cho ADMIN)
     * @param id ID của danh mục cần cập nhật
     * @param categoryRequest Thông tin mới của danh mục
     * @return CategoryResponse của danh mục đã cập nhật
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse updatedCategory = categoryService.updateCategory(id, categoryRequest);
        return ApiResponse.success(updatedCategory, "Category updated successfully");
    }
    
    /**
     * API xóa danh mục (chỉ dành cho ADMIN)
     * @param id ID của danh mục cần xóa
     * @return Thông báo xóa thành công
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success(null, "Category deleted successfully");
    }
}