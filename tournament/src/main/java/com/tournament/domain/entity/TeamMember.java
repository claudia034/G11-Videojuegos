package com.tournament.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "player_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "role_in_team")
    private String roleInTeam;
}
