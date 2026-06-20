package com.tournament.domain.entity;

import com.tournament.domain.enums.GenerationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brackets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bracket extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false, unique = true)
    private Tournament tournament;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_type", nullable = false)
    private GenerationType generationType;

    @Column(name = "is_complete", nullable = false)
    @Builder.Default
    private boolean complete = false;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @OneToMany(mappedBy = "bracket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("roundNumber ASC")
    @Builder.Default
    private List<Round> rounds = new ArrayList<>();
}
