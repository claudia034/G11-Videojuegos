package com.tournament.dto;

import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TournamentResponse(
        Long id,
        String name,
        String description,
        String gameName,
        TournamentFormat format,
        String formatDisplayName,
        TournamentStatus status,
        Integer maxParticipants,
        Integer currentParticipants,
        Boolean isTeamBased,
        Integer minElo,
        Integer maxElo,
        Long organizerId,
        LocalDateTime registrationStartAt,
        LocalDateTime registrationEndAt,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        BigDecimal totalPrizeValue,
        List<TournamentRoundResponse> rounds,
        List<TournamentPrizeResponse> prizes
) {
}
