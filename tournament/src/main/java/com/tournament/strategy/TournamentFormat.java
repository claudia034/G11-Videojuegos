package com.tournament.strategy;

import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;
import java.util.List;

public interface TournamentFormat {

    com.tournament.domain.enums.TournamentFormat getFormat();

    List<TournamentRound> generateRounds(Tournament tournament);
}
