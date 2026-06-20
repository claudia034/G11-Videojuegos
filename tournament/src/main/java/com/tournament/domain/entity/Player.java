package com.tournament.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "players")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Player extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "elo_rating", nullable = false)
    private int eloRating = 1000;
}
