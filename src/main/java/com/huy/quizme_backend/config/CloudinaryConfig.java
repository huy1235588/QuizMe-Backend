package com.huy.quizme_backend.config;

import com.cloudinary.Cloudinary;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "cloudinary")
@Getter
@Setter
public class CloudinaryConfig {
    private String cloudName;
    private String apiKey;
    private String apiSecret;
    private boolean secure;
    private String baseUrl;
    private Map<String, String> folder = new HashMap<>();

    @Bean
    public Cloudinary cloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", secure);
        return new Cloudinary(config);
    }

    // Getter specific for folders
    public String getProfileAvatarFolder() {
        return folder.get("profile-avatar");
    }

    public String getQuizThumbnailsFolder() {
        return folder.get("quiz-thumbnails");
    }

    public String getQuestionImagesFolder() {
        return folder.get("question-images");
    }

    public String getQuestionAudiosFolder() {
        return folder.get("question-audios");
    }

    public String getCategoryIconsFolder() {
        return folder.get("category-icons");
    }
}