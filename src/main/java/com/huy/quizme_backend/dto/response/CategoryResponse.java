package com.huy.quizme_backend.dto.response;

import com.huy.quizme_backend.enity.Category;
import com.huy.quizme_backend.service.LocalStorageService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private Integer quizCount;
    private Integer totalPlayCount;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
    
    // Chuyển đổi từ Category entity sang CategoryResponse DTO
    public static CategoryResponse fromCategory(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .quizCount(category.getQuizCount())
                .totalPlayCount(category.getTotalPlayCount())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt().toString())
                .updatedAt(category.getUpdatedAt() != null ? category.getUpdatedAt().toString() : null)
                .build();
    }
    
    // Chuyển đổi từ Category entity sang CategoryResponse DTO với Cloudinary URL
    public static CategoryResponse fromCategory(Category category, LocalStorageService localStorageService) {
        CategoryResponse response = fromCategory(category);
        if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
            response.setIconUrl(localStorageService.getCategoryIconUrl(category.getIconUrl()));
        }
        return response;
    }
}