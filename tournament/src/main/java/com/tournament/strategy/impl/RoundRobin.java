package com.tournament.strategy.impl;

import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.enums.TournamentFormat;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RoundRobin implements com.tournament.strategy.TournamentFormat {

    @Override
    public TournamentFormat getFormat() {
        return TournamentFormat.ROUND_ROBIN;
    }

    @Override
    public List<TournamentRound> generateRounds(Tournament tournament) {
        int participants = tournament.getMaxParticipants();
        int roundCount = participants % 2 == 0 ? participants - 1 : participants;
        List<TournamentRound> rounds = new ArrayList<>();

        for (int round = 1; round <= roundCount; round++) {
            rounds.add(TournamentRound.builder()
                    .roundNumber(round)
                    .name("Round Robin Round " + round)
                    .build());
        }

        return rounds;
    }
}
