package com.tournament.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_stats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlayerStats extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private Player player;

    @Column(nullable = false)
    @Builder.Default
    private int wins = 0;

    @Column(nullable = false)
    @Builder.Default
    private int losses = 0;

    @Column(name = "tournaments_played", nullable = false)
    @Builder.Default
    private int tournamentsPlayed = 0;

    @Column(name = "virtual_points", nullable = false)
    @Builder.Default
    private int virtualPoints = 0;

    public void incrementWins() {
        this.wins++;
    }

    public void incrementLosses() {
        this.losses++;
    }

    public void incrementTournamentsPlayed() {
        this.tournamentsPlayed++;
    }

    public void addVirtualPoints(int points) {
        this.virtualPoints += points;
    }
}
