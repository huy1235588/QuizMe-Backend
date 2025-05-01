package com.huy.quizme_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class LocalStorageService {
    @Value("${local-storage.base-path}")
    private String basePath;
    
    @Value("${local-storage.base-url}")
    private String baseUrl;
    
    private final String PROFILE_FOLDER = "profile-avatar";
    private final String QUIZ_THUMBNAILS_FOLDER = "quiz-thumbnails";
    private final String QUESTION_IMAGES_FOLDER = "question-images";
    private final String QUESTION_AUDIOS_FOLDER = "question-audios";
    private final String CATEGORY_ICONS_FOLDER = "category-icons";
    
    // URLs
    public String getProfileImageUrl(String filename) {
        return getResourceUrl(PROFILE_FOLDER, filename);
    }
    
    public String getQuizThumbnailUrl(String filename) {
        return getResourceUrl(QUIZ_THUMBNAILS_FOLDER, filename);
    }
    
    public String getQuestionImageUrl(String filename) {
        return getResourceUrl(QUESTION_IMAGES_FOLDER, filename);
    }
    
    public String getQuestionAudioUrl(String filename) {
        return getResourceUrl(QUESTION_AUDIOS_FOLDER, filename);
    }
    
    public String getCategoryIconUrl(String filename) {
        return getResourceUrl(CATEGORY_ICONS_FOLDER, filename);
    }
    
    private String getResourceUrl(String folder, String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return String.format("%s/%s/%s", baseUrl, folder, filename);
    }
    
    // Upload methods
    public String uploadProfileImage(MultipartFile file, long profileId) {
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = String.format("profile_%d_%d%s", profileId, System.currentTimeMillis(), extension);
        return uploadFile(file, filename, PROFILE_FOLDER);
    }
    
    public String uploadQuizThumbnail(MultipartFile file, long quizId) {
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = String.format("quiz_thumbnail_%d_%d%s", quizId, System.currentTimeMillis(), extension);
        return uploadFile(file, filename, QUIZ_THUMBNAILS_FOLDER);
    }
    
    public String uploadQuestionImage(MultipartFile file, long quizId, long questionId) {
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = String.format("quiz_%d_question_%d_%d%s", quizId, questionId, System.currentTimeMillis(), extension);
        return uploadFile(file, filename, QUESTION_IMAGES_FOLDER);
    }
    
    public String uploadQuestionAudio(MultipartFile file, long quizId, long questionId) {
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = String.format("quiz_%d_question_%d_%d%s", quizId, questionId, System.currentTimeMillis(), extension);
        return uploadFile(file, filename, QUESTION_AUDIOS_FOLDER);
    }
    
    public String uploadCategoryIcon(MultipartFile file, long categoryId) {
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = String.format("category_%d_%d%s", categoryId, System.currentTimeMillis(), extension);
        return uploadFile(file, filename, CATEGORY_ICONS_FOLDER);
    }
    
    private String uploadFile(MultipartFile file, String filename, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            // Tạo thư mục nếu chưa tồn tại
            Path folderPath = Paths.get(basePath, folder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
            
            // Lưu file
            Path filePath = Paths.get(basePath, folder, filename);
            Files.write(filePath, file.getBytes());
            
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }
    
    // Delete methods
    public boolean deleteProfileImage(String filename) {
        return deleteFile(filename, PROFILE_FOLDER);
    }
    
    public boolean deleteQuizThumbnail(String filename) {
        return deleteFile(filename, QUIZ_THUMBNAILS_FOLDER);
    }
    
    public boolean deleteQuestionImage(String filename) {
        return deleteFile(filename, QUESTION_IMAGES_FOLDER);
    }
    
    public boolean deleteQuestionAudio(String filename) {
        return deleteFile(filename, QUESTION_AUDIOS_FOLDER);
    }
    
    public boolean deleteCategoryIcon(String filename) {
        return deleteFile(filename, CATEGORY_ICONS_FOLDER);
    }
    
    private boolean deleteFile(String filename, String folder) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        // Extract filename from URL if needed
        if (filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf("/") + 1);
        }
        
        try {
            Path filePath = Paths.get(basePath, folder, filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}