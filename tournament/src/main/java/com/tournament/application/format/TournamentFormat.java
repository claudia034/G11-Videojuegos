package com.tournament.application.format;

import com.tournament.domain.entity.Bracket;
import com.tournament.domain.entity.Registration;

import java.util.List;

public interface TournamentFormat {
    BracketResult generateBracket(List<Registration> seeded, Bracket bracket);

    int getMinimumParticipants();
    boolean isComplete(Bracket bracket);
}
