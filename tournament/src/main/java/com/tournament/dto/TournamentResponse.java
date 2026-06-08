package com.tournament.dto;

import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentStatus;
import java.time.LocalDateTime;
import java.util.List;

public record TournamentResponse(
        Long id,
        String name,
        String description,
        String gameName,
        TournamentFormat format,
        TournamentStatus status,
        Integer maxParticipants,
        LocalDateTime registrationStartAt,
        LocalDateTime registrationEndAt,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<TournamentRoundResponse> rounds,
        List<TournamentPrizeResponse> prizes
) {
}
