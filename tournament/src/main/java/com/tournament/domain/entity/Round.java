package com.tournament.domain.entity;

import domain.enums.RoundSection;
import domain.enums.RoundStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rounds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Round extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bracket_id", nullable = false)
    private Bracket bracket;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(nullable = false)
    private String name;

    // Doble eliminacion
    @Enumerated(EnumType.STRING)
    private RoundSection section;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoundStatus status = RoundStatus.PENDING;

    @Column(name = "scheduled_start")
    private LocalDateTime scheduledStart;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<Match> matches = new ArrayList<>();
}

