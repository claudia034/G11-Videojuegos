package com.tournament.domain.entity;

import com.tournament.domain.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tournament_id", "player_id"}), // <-- Corregido a player_id
        @UniqueConstraint(columnNames = {"tournament_id", "team_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.CONFIRMED;

    @Column(name = "elo_at_registration")
    private Integer eloAtRegistration;

    private Integer seed;

    @CreatedDate
    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    @PreUpdate
    private void validateParticipant() {
        boolean hasPlayer = player != null;
        boolean hasTeam = team != null;

        if (hasPlayer == hasTeam) {
            throw new IllegalStateException(
                    "Error de integridad: La inscripción debe tener exactamente un participante (player XOR team)");
        }
    }

    public boolean isPlayerRegistration() {
        return player != null;
    }

    public boolean isTeamRegistration() {
        return team != null;
    }

    public String getParticipantName() {
        return isPlayerRegistration() ? player.getUsername() : team.getName();
    }

    public Long getParticipantId() {
        return isPlayerRegistration() ? player.getId() : team.getId();
    }

    public int getParticipantElo() {
        if (isPlayerRegistration()) {
            return player.getEloRating();
        }
        return (int) team.getMembers().stream()
                .mapToInt(m -> m.getPlayer().getEloRating())
                .average().orElse(1000);
    }
}