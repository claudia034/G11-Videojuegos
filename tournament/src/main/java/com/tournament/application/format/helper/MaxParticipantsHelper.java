package com.tournament.application.format.helper;

import com.tournament.application.format.TournamentFormatProfile;
import com.tournament.domain.entity.Tournament;

public class MaxParticipantsHelper {
    public static void participantConstraints(Tournament tournament, TournamentFormatProfile profile) {
        Integer maxParticipants = tournament.getMaxParticipants();

        if (maxParticipants == null || maxParticipants < profile.minimumParticipants()) {
            throw new IllegalArgumentException(
                    "El formato " + profile.displayName() + " requiere al menos "
                            + profile.minimumParticipants() + " participantes");
        }

        if (profile.maximumParticipants() != null
                && maxParticipants > profile.maximumParticipants()) {
            throw new IllegalArgumentException(
                    "El formato " + profile.displayName() + " soporta hasta "
                            + profile.maximumParticipants() + " participantes");
        }
    }
}
