package com.tournament.application.format;

import com.tournament.domain.entity.Bracket;
import com.tournament.domain.entity.Registration;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.enums.TournamentFormatFamily;
import com.tournament.domain.enums.TournamentRoundStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DoubleEliminationEngine implements TournamentFormatEngine {

    @Override
    public TournamentFormatFamily family() {
        return TournamentFormatFamily.DOUBLE_ELIMINATION;
    }

    @Override
    public List<TournamentRound> generateRounds(Tournament tournament, TournamentFormatProfile profile) {
        int winnersRounds = calculateEliminationRounds(tournament.getMaxParticipants());
        int losersRounds = Math.max(1, (winnersRounds * 2) - 2);
        List<TournamentRound> rounds = new ArrayList<>();
        int roundNumber = 1;

        for (int round = 1; round <= winnersRounds; round++) {
            rounds.add(TournamentRound.builder()
                    .roundNumber(roundNumber++)
                    .name("Winners round " + round)
                    .status(TournamentRoundStatus.PENDING)
                    .build());
        }

        for (int round = 1; round <= losersRounds; round++) {
            rounds.add(TournamentRound.builder()
                    .roundNumber(roundNumber++)
                    .name("Losers round " + round)
                    .status(TournamentRoundStatus.PENDING)
                    .build());
        }

        rounds.add(TournamentRound.builder()
                .roundNumber(roundNumber)
                .name("Grand final")
                .status(TournamentRoundStatus.PENDING)
                .build());

        return rounds;
    }

    @Override
    public BracketResult generateBracket(List<Registration> seeded, Bracket bracket, TournamentFormatProfile profile) {
        throw new IllegalArgumentException("El motor de doble eliminacion aun no soporta bracket automatico completo.");
    }

    private int calculateEliminationRounds(int participants) {
        int bracketSize = 1;
        while (bracketSize < participants) {
            bracketSize *= 2;
        }
        return Math.max(1, Integer.numberOfTrailingZeros(bracketSize));
    }
}
