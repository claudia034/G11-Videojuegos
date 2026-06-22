package com.tournament.application.format;

import com.tournament.domain.entity.Bracket;
import com.tournament.domain.entity.Registration;
import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;

import java.util.List;

public interface TournamentFormatStrategy {

    TournamentFormat getFormat();

    List<TournamentRound> generateRounds(Tournament tournament);

    BracketResult generateBracket(List<Registration> seeded, Bracket bracket);

    int getMinimumParticipants();
    boolean isComplete(Bracket bracket);
}
