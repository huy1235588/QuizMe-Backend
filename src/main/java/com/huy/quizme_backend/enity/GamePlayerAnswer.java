package com.huy.quizme_backend.enity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "game_player_answer",
        indexes = {
                @Index(name = "idx_game_result_participant", columnList = "game_result_id, participant_id"),
                @Index(name = "idx_participant_question", columnList = "participant_id, question_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GamePlayerAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_result_id", nullable = false)
    private GameResult gameResult;

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private RoomParticipant participant;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "is_correct")
    private boolean isCorrect = false;

    @Column(name = "answer_time", nullable = false)
    private Double answerTime;

    @Column(nullable = false)
    private int score;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "gamePlayerAnswer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GamePlayerAnswerOption> selectedOptions = new ArrayList<>();
}
