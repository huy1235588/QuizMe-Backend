package com.huy.quizme_backend.enity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "question",
        indexes = {
                @Index(name = "idx_quiz_id", columnList = "quiz_id"),
                @Index(name = "idx_order_number", columnList = "order_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "video_url", length = 255)
    private String videoUrl;

    @Column(name = "audio_url", length = 255)
    private String audioUrl;

    @Column(name = "fun_fact", columnDefinition = "TEXT")
    private String funFact;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "time_limit", nullable = false)
    private Integer timeLimit = 30;

    @Column(nullable = false)
    private Integer points = 10;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    @Column(name = "type", nullable = false)
    private QuestionType type = QuestionType.QUIZ;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<QuestionOption> options;
}