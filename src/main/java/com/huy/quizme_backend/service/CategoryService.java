package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.CategoryRequest;
import com.huy.quizme_backend.dto.response.CategoryResponse;
import com.huy.quizme_backend.enity.Category;
import com.huy.quizme_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    
    /**
     * Lấy danh sách tất cả các danh mục
     * @return Danh sách các CategoryResponse
     */
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> CategoryResponse.fromCategory(category, cloudinaryService))
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh mục theo ID
     * @param id ID của danh mục
     * @return CategoryResponse
     */
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found with id: " + id));
        return CategoryResponse.fromCategory(category, cloudinaryService);
    }
    
    /**
     * Tạo mới danh mục
     * @param categoryRequest thông tin danh mục cần tạo
     * @return CategoryResponse của danh mục đã tạo
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        // Kiểm tra xem danh mục với tên này đã tồn tại chưa
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category with name '" + categoryRequest.getName() + "' already exists");
        }
        
        // Tạo mới đối tượng Category
        Category category = Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .iconUrl(categoryRequest.getIconUrl())
                .quizCount(0)
                .totalPlayCount(0)
                .isActive(true)
                .build();
        
        // Lưu vào database
        Category savedCategory = categoryRepository.save(category);
        
        // Trả về response
        return CategoryResponse.fromCategory(savedCategory, cloudinaryService);
    }
    
    /**
     * Cập nhật thông tin danh mục
     * @param id ID của danh mục cần cập nhật
     * @param categoryRequest Thông tin mới của danh mục
     * @return CategoryResponse của danh mục đã cập nhật
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        // Tìm danh mục theo ID
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found with id: " + id));
        
        // Kiểm tra nếu tên danh mục thay đổi và tên mới đã tồn tại
        if (!category.getName().equals(categoryRequest.getName()) && 
                categoryRepository.existsByName(categoryRequest.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category with name '" + categoryRequest.getName() + "' already exists");
        }
        
        // Cập nhật thông tin
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setIconUrl(categoryRequest.getIconUrl());
        category.setIsActive(categoryRequest.getIsActive());
        
        // Lưu vào database
        Category updatedCategory = categoryRepository.save(category);
        
        // Trả về response
        return CategoryResponse.fromCategory(updatedCategory, cloudinaryService);
    }
    
    /**
     * Tạo tên file icon cho danh mục theo định dạng quy định
     * @param categoryId ID của danh mục
     * @return Tên file theo quy tắc
     */
    public String generateCategoryIconFilename(Long categoryId) {
        return cloudinaryService.generateCategoryIconFilename(categoryId);
    }
    
    /**
     * Xóa danh mục
     * @param id ID của danh mục cần xóa
     */
    @Transactional
    public void deleteCategory(Long id) {
        // Kiểm tra xem danh mục có tồn tại không
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Category not found with id: " + id);
        }
        
        // Xóa danh mục
        categoryRepository.deleteById(id);
    }
}