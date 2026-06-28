package com.tournament.application.dto.response;

import com.tournament.application.format.TournamentFormatProfile;
import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentFormatFamily;

public record TournamentFormatOptionDto(
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
    public static TournamentFormatOptionDto from(TournamentFormatProfile profile) {
        return new TournamentFormatOptionDto(
                profile.format(),
                profile.family(),
                profile.displayName(),
                profile.description(),
                profile.minimumParticipants(),
                profile.maximumParticipants(),
                profile.supportsBracketGeneration(),
                profile.supportsRankingSeeding(),
                profile.supportsLiveBracket(),
                profile.supportsRoundScheduling(),
                profile.defaultBestOf(),
                profile.roundRobinCycles(),
                profile.swissRounds()
        );
    }
}
