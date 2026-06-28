package com.tournament.dto;

import com.tournament.domain.enums.PrizeType;
import java.math.BigDecimal;

public record TournamentPrizeResponse(
        Long id,
        Integer position,
        String name,
        String description,
        PrizeType prizeType,
        BigDecimal amount,
        String currency,
        Long playerId,
        String playerUsername
) {
}
