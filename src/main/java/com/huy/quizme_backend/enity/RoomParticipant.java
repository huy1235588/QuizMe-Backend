package com.huy.quizme_backend.enity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_participant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private int score = 0;

    private boolean isHost = false;

    private LocalDateTime joinedAt = LocalDateTime.now();

    private LocalDateTime leftAt;

    private boolean isGuest = false;

    private String guestName;
}