package com.tournament.application.format;

import com.tournament.domain.entity.Bracket;
import com.tournament.domain.entity.Registration;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.enums.RoundStatus;
import com.tournament.domain.enums.TournamentFormatFamily;
import com.tournament.domain.enums.TournamentRoundStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SwissEngine implements TournamentFormatEngine {

    @Override
    public TournamentFormatFamily family() {
        return TournamentFormatFamily.SWISS;
    }

    @Override
    public List<TournamentRound> generateRounds(Tournament tournament, TournamentFormatProfile profile) {
        int totalRounds = profile.swissRounds() != null
                ? profile.swissRounds()
                : Math.max(1, (int) Math.ceil(Math.log(tournament.getMaxParticipants()) / Math.log(2)));

        List<TournamentRound> rounds = new ArrayList<>();
        for (int round = 1; round <= totalRounds; round++) {
            rounds.add(TournamentRound.builder()
                    .roundNumber(round)
                    .name("Swiss round " + round)
                    .status(TournamentRoundStatus.PENDING)
                    .build());
        }
        return rounds;
    }

    @Override
    public BracketResult generateBracket(List<Registration> seeded, Bracket bracket, TournamentFormatProfile profile) {
        throw new IllegalArgumentException("El motor Swiss aun no soporta emparejamientos automaticos completos.");
    }

    @Override
    public boolean isComplete(Bracket bracket, TournamentFormatProfile profile) {
        return bracket.getRounds().stream()
                .allMatch(round -> round.getStatus() == RoundStatus.COMPLETED);
    }
}
