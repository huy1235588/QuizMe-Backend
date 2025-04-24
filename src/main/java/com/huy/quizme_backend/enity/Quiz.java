package com.huy.quizme_backend.enity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "quiz",
    indexes = {
        @Index(name = "idx_category_id", columnList = "category_id"),
        @Index(name = "idx_creator_id", columnList = "creator_id"),
        @Index(name = "idx_difficulty", columnList = "difficulty"),
        @Index(name = "idx_is_public", columnList = "is_public")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "quiz_thumbnails", length = 255)
    private String quizThumbnails;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "play_count", nullable = false)
    private Integer playCount = 0;

    @Column(name = "question_count", nullable = false)
    private Integer questionCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}