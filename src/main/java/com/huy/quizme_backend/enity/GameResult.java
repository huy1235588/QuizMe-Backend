package com.huy.quizme_backend.enity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "game_result",
        indexes = {
                @Index(name = "idx_room_id", columnList = "room_id"),
                @Index(name = "idx_quiz_id", columnList = "quiz_id"),
                @Index(name = "idx_start_time", columnList = "start_time")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "start_time")
    private LocalDateTime startTime = LocalDateTime.now();

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "participant_count", nullable = false)
    private int participantCount;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @Column(name = "avg_score")
    private Float avgScore;

    @Column(name = "highest_score")
    private Integer highestScore;

    @Column(name = "lowest_score")
    private Integer lowestScore;

    @Column(name = "completion_rate")
    private Float completionRate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "gameResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameResultQuestion> gameResultQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "gameResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GamePlayerAnswer> gamePlayerAnswers = new ArrayList<>();
}
