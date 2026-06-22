package com.tournament.strategy.impl;

import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.enums.TournamentFormat;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class Swiss implements com.tournament.strategy.TournamentFormat {

    @Override
    public TournamentFormat getFormat() {
        return TournamentFormat.SWISS;
    }

    @Override
    public List<TournamentRound> generateRounds(Tournament tournament) {
        int roundCount = calculateSwissRounds(tournament.getMaxParticipants());
        List<TournamentRound> rounds = new ArrayList<>();

        for (int round = 1; round <= roundCount; round++) {
            rounds.add(TournamentRound.builder()
                    .roundNumber(round)
                    .name("Swiss Round " + round)
                    .build());
        }

        return rounds;
    }

    private int calculateSwissRounds(int participants) {
        return Math.max(1, (int) Math.ceil(Math.log(participants) / Math.log(2)));
    }
}
