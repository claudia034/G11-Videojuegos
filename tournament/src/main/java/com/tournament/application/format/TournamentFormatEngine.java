package com.tournament.application.format;

import com.tournament.domain.entity.Bracket;
import com.tournament.domain.entity.Registration;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.enums.TournamentFormatFamily;

import java.util.List;

public interface TournamentFormatEngine {

    TournamentFormatFamily family();

    List<TournamentRound> generateRounds(Tournament tournament, TournamentFormatProfile profile);

    BracketResult generateBracket(List<Registration> seeded, Bracket bracket, TournamentFormatProfile profile);

    default boolean isComplete(Bracket bracket, TournamentFormatProfile profile) {
        return bracket.isComplete();
    }

    default void validateTournamentConfiguration(Tournament tournament, TournamentFormatProfile profile) {
        Integer maxParticipants = tournament.getMaxParticipants();

        if (maxParticipants == null || maxParticipants < profile.minimumParticipants()) {
            throw new IllegalArgumentException(
                    "El formato " + profile.displayName() + " requiere al menos "
                            + profile.minimumParticipants() + " participantes");
        }

        if (profile.maximumParticipants() != null && maxParticipants > profile.maximumParticipants()) {
            throw new IllegalArgumentException(
                    "El formato " + profile.displayName() + " soporta hasta "
                            + profile.maximumParticipants() + " participantes");
        }
    }
}
