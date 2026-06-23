package com.tournament.dto;

import com.tournament.domain.enums.PrizeType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateTournamentPrizeRequest(
        @NotNull
        @Min(1)
        Integer position,

        @NotBlank
        @Size(max = 120)
        String name,

        @Size(max = 1000)
        String description,

        PrizeType prizeType,

        @PositiveOrZero
        @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        @Size(min = 3, max = 3)
        String currency,

        Long playerId
) {
}
