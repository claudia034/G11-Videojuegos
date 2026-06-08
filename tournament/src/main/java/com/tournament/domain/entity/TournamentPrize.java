package com.tournament.domain.entity;

import com.tournament.domain.enums.PrizeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "tournament_prizes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tournament_prizes_tournament_position",
                columnNames = {"tournament_id", "prize_position"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentPrize extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "tournament_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_tournament_prizes_tournament")
    )
    private Tournament tournament;

    @NotNull
    @Min(1)
    @Column(name = "prize_position", nullable = false)
    private Integer position;

    @NotBlank
    @Size(max = 120)
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "prize_type", nullable = false, length = 30)
    @Builder.Default
    private PrizeType prizeType = PrizeType.OTHER;

    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Size(min = 3, max = 3)
    @Column(name = "currency", length = 3)
    private String currency;
}
