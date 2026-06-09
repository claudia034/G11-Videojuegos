package com.tournament.domain.entity;

import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tournament extends BaseEntity {

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotBlank
    @Size(max = 100)
    @Column(name = "game_name", nullable = false, length = 100)
    private String gameName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tournament_format", nullable = false, length = 30)
    @Builder.Default
    private TournamentFormat format = TournamentFormat.SINGLE_ELIMINATION;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private TournamentStatus status = TournamentStatus.DRAFT;

    @NotNull
    @Min(2)
    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @NotNull
    @Column(name = "is_team_based", nullable = false)
    @Builder.Default
    private Boolean isTeamBased = false;

    @Min(0)
    @Column(name = "min_elo")
    private Integer minElo;

    @Min(0)
    @Column(name = "max_elo")
    private Integer maxElo;

    @Column(name = "organizer_id")
    private Long organizerId;

    @Column(name = "registration_start_at")
    private LocalDateTime registrationStartAt;

    @Column(name = "registration_end_at")
    private LocalDateTime registrationEndAt;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("roundNumber ASC")
    @Builder.Default
    private List<TournamentRound> rounds = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<TournamentPrize> prizes = new ArrayList<>();

    public void addRound(TournamentRound round) {
        rounds.add(round);
        round.setTournament(this);
    }

    public void removeRound(TournamentRound round) {
        rounds.remove(round);
        round.setTournament(null);
    }

    public void addPrize(TournamentPrize prize) {
        prizes.add(prize);
        prize.setTournament(this);
    }

    public void removePrize(TournamentPrize prize) {
        prizes.remove(prize);
        prize.setTournament(null);
    }
}
