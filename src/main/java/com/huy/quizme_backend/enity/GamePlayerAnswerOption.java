package com.huy.quizme_backend.enity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "game_player_answer_option",
        indexes = {
                @Index(name = "idx_game_player_answer_id", columnList = "game_player_answer_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_player_answer_option",
                        columnNames = {"game_player_answer_id", "option_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GamePlayerAnswerOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_player_answer_id", nullable = false)
    private GamePlayerAnswer gamePlayerAnswer;

    @ManyToOne
    @JoinColumn(name = "option_id", nullable = false)
    private QuestionOption option;
}
