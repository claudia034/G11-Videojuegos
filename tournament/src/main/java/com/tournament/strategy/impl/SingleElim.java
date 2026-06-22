package com.tournament.strategy.impl;

import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.enums.TournamentFormat;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SingleElim implements com.tournament.strategy.TournamentFormat {

    @Override
    public TournamentFormat getFormat() {
        return TournamentFormat.SINGLE_ELIMINATION;
    }

    @Override
    public List<TournamentRound> generateRounds(Tournament tournament) {
        int bracketSize = calculateBracketSize(tournament.getMaxParticipants());
        int totalRounds = Math.max(1, Integer.numberOfTrailingZeros(bracketSize));
        List<TournamentRound> rounds = new ArrayList<>();

        for (int round = 1; round <= totalRounds; round++) {
            int participantsInRound = bracketSize / (int) Math.pow(2, round - 1);
            rounds.add(TournamentRound.builder()
                    .roundNumber(round)
                    .name(resolveRoundName(participantsInRound))
                    .build());
        }

        return rounds;
    }

    private int calculateBracketSize(int participants) {
        int bracketSize = 1;
        while (bracketSize < participants) {
            bracketSize *= 2;
        }
        return bracketSize;
    }

    private String resolveRoundName(int participantsInRound) {
        return switch (participantsInRound) {
            case 2 -> "Final";
            case 4 -> "Semifinals";
            case 8 -> "Quarterfinals";
            default -> "Round of " + participantsInRound;
        };
    }
}
