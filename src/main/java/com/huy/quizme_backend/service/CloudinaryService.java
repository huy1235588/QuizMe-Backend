package com.huy.quizme_backend.service;

import com.cloudinary.Cloudinary;
import com.huy.quizme_backend.config.CloudinaryConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service chịu trách nhiệm xử lý URL hình ảnh Cloudinary
 */
@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final CloudinaryConfig cloudinaryConfig;
    private final Cloudinary cloudinary;
    
    /**
     * Enum định nghĩa các loại file và thông tin liên quan
     */
    @Getter
    public enum FileType {
        PROFILE("profile_%d_%d%s", "getProfileAvatarFolder"),
        QUIZ_THUMBNAIL("quiz_thumbnail_%d_%d%s", "getQuizThumbnailsFolder"),
        QUESTION_IMAGE("quiz_%d_question_%d_%d%s", "getQuestionImagesFolder"),
        CATEGORY_ICON("category_%d_%d%s", "getCategoryIconsFolder");
        
        private final String filenamePattern;
        private final String folderMethod;
        
        FileType(String filenamePattern, String folderMethod) {
            this.filenamePattern = filenamePattern;
            this.folderMethod = folderMethod;
        }
    }

    /**
     * Tạo URL đầy đủ cho ảnh đại diện của người dùng
     *
     * @param filename Tên file được lưu trong database
     * @return URL đầy đủ của Cloudinary
     */
    public String getProfileImageUrl(String filename) {
        return getResourceUrl(cloudinaryConfig.getProfileAvatarFolder(), filename);
    }

    /**
     * Tạo URL đầy đủ cho ảnh thumbnail của quiz
     *
     * @param filename Tên file được lưu trong database
     * @return URL đầy đủ của Cloudinary
     */
    public String getQuizThumbnailUrl(String filename) {
        return getResourceUrl(cloudinaryConfig.getQuizThumbnailsFolder(), filename);
    }

    /**
     * Tạo URL đầy đủ cho ảnh của câu hỏi
     *
     * @param filename Tên file được lưu trong database
     * @return URL đầy đủ của Cloudinary
     */
    public String getQuestionImageUrl(String filename) {
        return getResourceUrl(cloudinaryConfig.getQuestionImagesFolder(), filename);
    }

    /**
     * Tạo URL đầy đủ cho icon của danh mục
     *
     * @param filename Tên file được lưu trong database
     * @return URL đầy đủ của Cloudinary
     */
    public String getCategoryIconUrl(String filename) {
        return getResourceUrl(cloudinaryConfig.getCategoryIconsFolder(), filename);
    }

    /**
     * Tạo URL chung cho tất cả loại tài nguyên
     *
     * @param folder Thư mục chứa tài nguyên
     * @param filename Tên file
     * @return URL đầy đủ hoặc null nếu filename không hợp lệ
     */
    private String getResourceUrl(String folder, String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return buildCloudinaryUrl(folder, filename);
    }

    /**
     * Tạo đường dẫn Cloudinary đầy đủ
     *
     * @param folder   Tên thư mục
     * @param filename Tên file
     * @return URL đầy đủ
     */
    private String buildCloudinaryUrl(String folder, String filename) {
        return String.format("%s%s/%s/%s/%s",
                cloudinaryConfig.getBaseUrl(),
                cloudinaryConfig.getCloudName(),
                "image/upload",
                folder,
                filename);
    }

    /**
     * Tạo tên file theo định dạng cho profile avatar
     *
     * @param profileId ID của profile
     * @param extension Phần mở rộng của file (ví dụ: .jpg, .png)
     * @return Tên file theo quy tắc
     */
    public String generateProfileImageFilename(Long profileId, String extension) {
        return String.format(FileType.PROFILE.getFilenamePattern(), 
                profileId, System.currentTimeMillis(), extension);
    }

    /**
     * Tạo tên file theo định dạng cho quiz thumbnail
     *
     * @param quizId ID của quiz
     * @param extension Phần mở rộng của file
     * @return Tên file theo quy tắc
     */
    public String generateQuizThumbnailFilename(Long quizId, String extension) {
        return String.format(FileType.QUIZ_THUMBNAIL.getFilenamePattern(), 
                quizId, System.currentTimeMillis(), extension);
    }

    /**
     * Tạo tên file theo định dạng cho question image
     *
     * @param quizId     ID của quiz
     * @param questionId ID của câu hỏi
     * @param extension Phần mở rộng của file
     * @return Tên file theo quy tắc
     */
    public String generateQuestionImageFilename(Long quizId, Long questionId, String extension) {
        return String.format(FileType.QUESTION_IMAGE.getFilenamePattern(), 
                quizId, questionId, System.currentTimeMillis(), extension);
    }

    /**
     * Tạo tên file theo định dạng cho category icon
     *
     * @param categoryId ID của danh mục
     * @param extension Phần mở rộng của file
     * @return Tên file theo quy tắc
     */
    public String generateCategoryIconFilename(Long categoryId, String extension) {
        return String.format(FileType.CATEGORY_ICON.getFilenamePattern(), 
                categoryId, System.currentTimeMillis(), extension);
    }

    /**
     * Upload ảnh đại diện người dùng lên Cloudinary
     * 
     * @param file File ảnh
     * @param profileId ID của profile
     * @return Tên file đã tạo hoặc null nếu upload thất bại
     */
    public String uploadProfileImage(MultipartFile file, long profileId) {
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = generateProfileImageFilename(profileId, extension);
        return uploadFile(file, filename, cloudinaryConfig.getProfileAvatarFolder());
    }

    /**
     * Upload ảnh thumbnail quiz lên Cloudinary
     * 
     * @param file File ảnh
     * @param quizId ID của quiz
     * @return Tên file đã tạo hoặc null nếu upload thất bại
     */
    public String uploadQuizThumbnail(MultipartFile file, long quizId) {
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = generateQuizThumbnailFilename(quizId, extension);
        return uploadFile(file, filename, cloudinaryConfig.getQuizThumbnailsFolder());
    }
    
    /**
     * Upload ảnh câu hỏi lên Cloudinary
     * 
     * @param file File ảnh
     * @param quizId ID của quiz
     * @param questionId ID của câu hỏi
     * @return Tên file đã tạo hoặc null nếu upload thất bại
     */
    public String uploadQuestionImage(MultipartFile file, long quizId, long questionId) {
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = generateQuestionImageFilename(quizId, questionId, extension);
        return uploadFile(file, filename, cloudinaryConfig.getQuestionImagesFolder());
    }

    /**
     * Upload icon danh mục lên Cloudinary
     *
     * @param iconFile File icon
     * @param categoryId ID của danh mục
     * @return Tên file đã tạo hoặc null nếu upload thất bại
     */
    public String uploadCategoryIcon(MultipartFile iconFile, long categoryId) {
        String extension = getFileExtension(iconFile.getOriginalFilename());
        String filename = generateCategoryIconFilename(categoryId, extension);
        return uploadFile(iconFile, filename, cloudinaryConfig.getCategoryIconsFolder());
    }

    /**
     * Upload file lên Cloudinary
     * 
     * @param file File cần upload
     * @param filename Tên file đã được tạo
     * @param folder Thư mục chứa file
     * @return Tên file hoặc null nếu thất bại
     */
    private String uploadFile(MultipartFile file, String filename, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String publicId = filename.substring(0, filename.lastIndexOf("."));
            String resourceType = determineResourceType(getFileExtension(filename));
            
            Map<String, Object> params = new HashMap<>();
            params.put("folder", folder);
            params.put("public_id", publicId);
            params.put("resource_type", resourceType);
            
            cloudinary.uploader().upload(file.getBytes(), params);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy phần mở rộng từ tên file
     * @param filename Tên file đầy đủ
     * @return Phần mở rộng bao gồm dấu chấm
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Xác định resource_type dựa vào phần mở rộng file
     * @param extension Phần mở rộng của file
     * @return "image", "video" hoặc "raw" tùy thuộc vào loại file
     */
    private String determineResourceType(String extension) {
        if (extension == null) {
            return "image";
        }

        extension = extension.toLowerCase();

        if (extension.matches("\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
            return "image";
        } else if (extension.matches("\\.(mp4|mov|avi|wmv)$")) {
            return "video";
        } else {
            return "raw";
        }
    }
    
    /**
     * Xóa file từ Cloudinary dựa trên tên file và folder chứa
     * 
     * @param filename Tên file cần xóa
     * @param folder Thư mục chứa file
     * @return true nếu xóa thành công, false nếu có lỗi
     */
    public boolean deleteFile(String filename, String folder) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        try {
            // Lấy public_id từ filename (bỏ phần extension)
            String publicId = filename;
            if (filename.contains(".")) {
                publicId = filename.substring(0, filename.lastIndexOf("."));
            }
            
            // Tạo public_id đầy đủ với folder
            String fullPublicId = folder + "/" + publicId;
            
            // Xóa file từ Cloudinary
            Map<String, Object> params = new HashMap<>();
            params.put("resource_type", "image"); // Mặc định là image, có thể điều chỉnh dựa trên loại file
            
            Map result = cloudinary.uploader().destroy(fullPublicId, params);
            
            // Kiểm tra kết quả
            return "ok".equals(result.get("result"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from Cloudinary: " + e.getMessage(), e);
        }
    }
    
    /**
     * Xóa icon của danh mục từ Cloudinary
     * 
     * @param iconUrl URL hoặc tên file của icon
     * @return true nếu xóa thành công, false nếu có lỗi
     */
    public boolean deleteCategoryIcon(String iconUrl) {
        // Nếu là URL đầy đủ, trích xuất tên file
        String filename = iconUrl;
        if (iconUrl != null && iconUrl.contains("/")) {
            filename = iconUrl.substring(iconUrl.lastIndexOf("/") + 1);
        }
        
        return deleteFile(filename, cloudinaryConfig.getCategoryIconsFolder());
    }
}