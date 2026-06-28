package com.tournament.application.format;

import com.tournament.domain.entity.Bracket;
import com.tournament.domain.entity.Match;
import com.tournament.domain.entity.Registration;
import com.tournament.domain.entity.Round;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.enums.MatchStatus;
import com.tournament.domain.enums.RoundStatus;
import com.tournament.domain.enums.TournamentFormatFamily;
import com.tournament.domain.enums.TournamentRoundStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class RoundRobinEngine implements TournamentFormatEngine {

    @Override
    public TournamentFormatFamily family() {
        return TournamentFormatFamily.ROUND_ROBIN;
    }

    @Override
    public List<TournamentRound> generateRounds(Tournament tournament, TournamentFormatProfile profile) {
        int participants = tournament.getMaxParticipants();
        int totalRounds = calculateRoundCount(participants, profile.roundRobinCycles());
        List<TournamentRound> rounds = new ArrayList<>();

        for (int round = 1; round <= totalRounds; round++) {
            rounds.add(TournamentRound.builder()
                    .roundNumber(round)
                    .name("Round robin round " + round)
                    .status(TournamentRoundStatus.PENDING)
                    .build());
        }
        return rounds;
    }

    @Override
    public BracketResult generateBracket(List<Registration> seeded, Bracket bracket, TournamentFormatProfile profile) {
        List<Registration> competitors = new ArrayList<>(seeded);
        if (competitors.size() % 2 != 0) {
            competitors.add(null);
        }

        int baseRounds = competitors.size() - 1;
        int matchesPerRound = competitors.size() / 2;
        List<Round> rounds = new ArrayList<>();
        List<Match> matches = new ArrayList<>();
        List<Registration> rotation = new ArrayList<>(competitors);

        for (int cycle = 0; cycle < profile.roundRobinCycles(); cycle++) {
            for (int roundIndex = 0; roundIndex < baseRounds; roundIndex++) {
                Round round = Round.builder()
                        .bracket(bracket)
                        .roundNumber(rounds.size() + 1)
                        .name("Round robin round " + (rounds.size() + 1))
                        .status(RoundStatus.PENDING)
                        .build();
                bracket.getRounds().add(round);
                rounds.add(round);

                for (int matchIndex = 0; matchIndex < matchesPerRound; matchIndex++) {
                    Registration home = rotation.get(matchIndex);
                    Registration away = rotation.get(rotation.size() - 1 - matchIndex);

                    if (home == null || away == null) {
                        continue;
                    }

                    Match match = Match.builder()
                            .round(round)
                            .position(matchIndex + 1)
                            .registration1(cycle % 2 == 0 ? home : away)
                            .registration2(cycle % 2 == 0 ? away : home)
                            .status(MatchStatus.SCHEDULED)
                            .bestOf(profile.defaultBestOf())
                            .build();
                    round.getMatches().add(match);
                    matches.add(match);
                }

                rotate(rotation);
            }
        }

        return new BracketResult(rounds, matches);
    }

    @Override
    public boolean isComplete(Bracket bracket, TournamentFormatProfile profile) {
        return bracket.getRounds().stream()
                .flatMap(round -> round.getMatches().stream())
                .allMatch(match -> match.getStatus() == MatchStatus.COMPLETED);
    }

    private int calculateRoundCount(int participants, int cycles) {
        int normalizedParticipants = participants % 2 == 0 ? participants : participants + 1;
        return Math.max(1, (normalizedParticipants - 1) * Math.max(1, cycles));
    }

    private void rotate(List<Registration> rotation) {
        if (rotation.size() <= 2) {
            return;
        }

        Registration fixed = rotation.get(0);
        List<Registration> movable = new ArrayList<>(rotation.subList(1, rotation.size()));
        Collections.rotate(movable, 1);

        rotation.clear();
        rotation.add(fixed);
        rotation.addAll(movable);
    }
}
