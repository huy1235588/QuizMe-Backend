package com.huy.quizme_backend.enity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 100)
    private String city;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "total_score")
    private Integer totalScore = 0;

    @Column(name = "quizzes_played")
    private Integer quizzesPlayed = 0;

    @Column(name = "quizzes_created")
    private Integer quizzesCreated = 0;

    @Column(name = "total_quiz_plays")
    private Integer totalQuizPlays = 0;

}