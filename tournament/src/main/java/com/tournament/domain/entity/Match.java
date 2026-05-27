package domain.entity;

import domain.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Match extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration1_id")
    private Registration registration1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration2_id")
    private Registration registration2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_match_id")
    private Match nextMatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loser_next_match_id")
    private Match loserNextMatch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MatchStatus status = MatchStatus.SCHEDULED;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "best_of")
    @Builder.Default
    private Integer bestOf = 1;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    public boolean isBye() {
        return registration1 == null || registration2 == null;
    }

    public Registration getByeWinner() {
        if (!isBye()) throw new IllegalStateException("No es un BYE");
        return registration1 != null ? registration1 : registration2;
    }
}
