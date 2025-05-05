package com.huy.quizme_backend.enity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    public enum Status {
        waiting, in_progress, completed, cancelled
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    private int maxPlayers = 10;

    @Enumerated(EnumType.STRING)
    private Status status = Status.waiting;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<RoomParticipant> participants = new ArrayList<>();
}
