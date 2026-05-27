package domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "registration_rosters",
        uniqueConstraints = @UniqueConstraint(columnNames = {"registration_id", "player_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegistrationRoster extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "role_in_team", nullable = false)
    private String roleInTeam;

    @Column(name = "elo_at_registration", nullable = false)
    private Integer eloAtRegistration;
}
