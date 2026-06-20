package com.tournament.domain.entity;

import com.tournament.domain.enums.FormatType;
import com.tournament.domain.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tournaments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tournament extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TournamentStatus status = TournamentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FormatType format = FormatType.SINGLE_ELIMINATION;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "min_elo")
    private Integer minElo;

    @Column(name = "max_elo")
    private Integer maxElo;

    @Column(name = "is_team_based", nullable = false)
    @Builder.Default
    private boolean teamBased = false;
}
