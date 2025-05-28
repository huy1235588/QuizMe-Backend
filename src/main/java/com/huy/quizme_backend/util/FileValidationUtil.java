package com.huy.quizme_backend.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class để xử lý validation file upload
 */
public class FileValidationUtil {

    // Các định dạng ảnh được phép
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    // Kích thước file tối đa (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Kiểm tra xem file có phải là ảnh hợp lệ không
     *
     * @param file File cần kiểm tra
     * @return true nếu file hợp lệ, false nếu không
     */
    public static boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Kiểm tra content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return false;
        }

        // Kiểm tra kích thước file
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        // Kiểm tra tên file
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Lấy thông báo lỗi chi tiết cho file không hợp lệ
     *
     * @param file File cần kiểm tra
     * @return Thông báo lỗi
     */
    public static String getImageValidationError(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "File is required";
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return "Only image files (JPEG, PNG, GIF, WebP) are allowed";
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return "File size must be less than 5MB";
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            return "Invalid file name";
        }

        return null; // No error
    }

    /**
     * Lấy extension từ filename
     *
     * @param filename Tên file
     * @return Extension của file (bao gồm dấu chấm)
     */
    public static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    /**
     * Kiểm tra xem extension có hợp lệ không
     *
     * @param extension Extension cần kiểm tra
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidImageExtension(String extension) {
        if (extension == null) {
            return false;
        }

        List<String> validExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
        return validExtensions.contains(extension.toLowerCase());
    }
}
