package com.huy.quizme_backend.enity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "game_result_question",
        indexes = {
                @Index(name = "idx_game_result_id", columnList = "game_result_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResultQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_result_id", nullable = false)
    private GameResult gameResult;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "incorrect_count", nullable = false)
    private int incorrectCount;

    @Column(name = "avg_time")
    private Float avgTime;
}
