package com.tournament.application.format;

import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentFormatFamily;

public record TournamentFormatProfile(
        TournamentFormat format,
        TournamentFormatFamily family,
        String displayName,
        String description,
        int minimumParticipants,
        Integer maximumParticipants,
        boolean supportsBracketGeneration,
        boolean supportsRankingSeeding,
        boolean supportsLiveBracket,
        boolean supportsRoundScheduling,
        int defaultBestOf,
        int roundRobinCycles,
        Integer swissRounds
) {
}
