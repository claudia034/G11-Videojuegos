package com.tournament.dto;

import com.tournament.domain.enums.TournamentRoundStatus;
import java.time.LocalDateTime;

public record TournamentRoundResponse(
        Long id,
        Integer roundNumber,
        String name,
        TournamentRoundStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
