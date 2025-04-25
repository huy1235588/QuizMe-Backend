package com.huy.quizme_backend.service;

import com.huy.quizme_backend.config.CloudinaryConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service chịu trách nhiệm xử lý URL hình ảnh Cloudinary
 */
@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final CloudinaryConfig cloudinaryConfig;
    
    /**
     * Tạo URL đầy đủ cho ảnh đại diện của người dùng
     * @param filename Tên file được lưu trong database (ví dụ: profile_123_1680000000.jpg)
     * @return URL đầy đủ của Cloudinary
     */
    public String getProfileImageUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return buildCloudinaryUrl(cloudinaryConfig.getProfileAvatarFolder(), filename);
    }
    
    /**
     * Tạo URL đầy đủ cho ảnh thumbnail của quiz
     * @param filename Tên file được lưu trong database
     * @return URL đầy đủ của Cloudinary
     */
    public String getQuizThumbnailUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return buildCloudinaryUrl(cloudinaryConfig.getQuizThumbnailsFolder(), filename);
    }
    
    /**
     * Tạo URL đầy đủ cho ảnh của câu hỏi
     * @param filename Tên file được lưu trong database
     * @return URL đầy đủ của Cloudinary
     */
    public String getQuestionImageUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return buildCloudinaryUrl(cloudinaryConfig.getQuestionImagesFolder(), filename);
    }
    
    /**
     * Tạo URL đầy đủ cho icon của danh mục
     * @param filename Tên file được lưu trong database
     * @return URL đầy đủ của Cloudinary
     */
    public String getCategoryIconUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return buildCloudinaryUrl(cloudinaryConfig.getCategoryIconsFolder(), filename);
    }
    
    /**
     * Tạo đường dẫn Cloudinary đầy đủ
     * @param folder Tên thư mục
     * @param filename Tên file
     * @return URL đầy đủ
     */
    private String buildCloudinaryUrl(String folder, String filename) {
        return String.format("%s%s/%s/%s",
                cloudinaryConfig.getBaseUrl(),
                cloudinaryConfig.getCloudName(),
                "image/upload",
                filename);
    }
    
    /**
     * Tạo tên file theo định dạng cho profile avatar
     * @param profileId ID của profile
     * @return Tên file theo quy tắc
     */
    public String generateProfileImageFilename(Long profileId) {
        return String.format("profile_%d_%d.jpg", profileId, System.currentTimeMillis());
    }
    
    /**
     * Tạo tên file theo định dạng cho quiz thumbnail
     * @param quizId ID của quiz
     * @return Tên file theo quy tắc
     */
    public String generateQuizThumbnailFilename(Long quizId) {
        return String.format("quiz_%d_%d.jpg", quizId, System.currentTimeMillis());
    }
    
    /**
     * Tạo tên file theo định dạng cho question image
     * @param quizId ID của quiz
     * @param questionId ID của câu hỏi
     * @return Tên file theo quy tắc
     */
    public String generateQuestionImageFilename(Long quizId, Long questionId) {
        return String.format("quiz_%d_question_%d_%d.jpg", quizId, questionId, System.currentTimeMillis());
    }
    
    /**
     * Tạo tên file theo định dạng cho category icon
     * @param categoryId ID của danh mục
     * @return Tên file theo quy tắc
     */
    public String generateCategoryIconFilename(Long categoryId) {
        return String.format("category_%d_%d.jpg", categoryId, System.currentTimeMillis());
    }
}