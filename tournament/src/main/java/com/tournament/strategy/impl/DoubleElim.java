package com.tournament.strategy.impl;

import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.enums.TournamentFormat;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DoubleElim implements com.tournament.strategy.TournamentFormat {

    @Override
    public TournamentFormat getFormat() {
        return TournamentFormat.DOUBLE_ELIMINATION;
    }

    @Override
    public List<TournamentRound> generateRounds(Tournament tournament) {
        int winnersRounds = calculateEliminationRounds(tournament.getMaxParticipants());
        int losersRounds = Math.max(1, (winnersRounds * 2) - 2);
        List<TournamentRound> rounds = new ArrayList<>();
        int roundNumber = 1;

        for (int round = 1; round <= winnersRounds; round++) {
            rounds.add(buildRound(roundNumber++, "Winners Round " + round));
        }

        for (int round = 1; round <= losersRounds; round++) {
            rounds.add(buildRound(roundNumber++, "Losers Round " + round));
        }

        rounds.add(buildRound(roundNumber, "Grand Final"));
        return rounds;
    }

    private int calculateEliminationRounds(int participants) {
        int bracketSize = 1;
        while (bracketSize < participants) {
            bracketSize *= 2;
        }
        return Math.max(1, Integer.numberOfTrailingZeros(bracketSize));
    }

    private TournamentRound buildRound(int roundNumber, String name) {
        return TournamentRound.builder()
                .roundNumber(roundNumber)
                .name(name)
                .build();
    }
}
