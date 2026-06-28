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
import java.util.Comparator;
import java.util.List;

@Component
public class SingleEliminationEngine implements TournamentFormatEngine {

    @Override
    public TournamentFormatFamily family() {
        return TournamentFormatFamily.SINGLE_ELIMINATION;
    }

    @Override
    public List<TournamentRound> generateRounds(Tournament tournament, TournamentFormatProfile profile) {
        int size = nextPowerOfTwo(tournament.getMaxParticipants());
        int totalRounds = (int) (Math.log(size) / Math.log(2));

        List<TournamentRound> rounds = new ArrayList<>();
        for (int roundNumber = 1; roundNumber <= totalRounds; roundNumber++) {
            rounds.add(TournamentRound.builder()
                    .roundNumber(roundNumber)
                    .name(resolveRoundName(totalRounds, roundNumber))
                    .status(TournamentRoundStatus.PENDING)
                    .build());
        }
        return rounds;
    }

    @Override
    public BracketResult generateBracket(List<Registration> seeded, Bracket bracket, TournamentFormatProfile profile) {
        int bracketSize = nextPowerOfTwo(Math.max(2, seeded.size()));
        int totalRounds = (int) (Math.log(bracketSize) / Math.log(2));

        List<Round> rounds = new ArrayList<>();
        List<List<Match>> matchesByRound = new ArrayList<>();

        for (int roundIndex = 0; roundIndex < totalRounds; roundIndex++) {
            int matchCount = bracketSize >> (roundIndex + 1);
            Round round = Round.builder()
                    .bracket(bracket)
                    .roundNumber(roundIndex + 1)
                    .name(resolveRoundName(totalRounds, roundIndex + 1))
                    .status(RoundStatus.PENDING)
                    .build();
            bracket.getRounds().add(round);
            rounds.add(round);

            List<Match> roundMatches = new ArrayList<>();
            for (int matchIndex = 0; matchIndex < matchCount; matchIndex++) {
                Match match = Match.builder()
                        .round(round)
                        .position(matchIndex + 1)
                        .status(MatchStatus.SCHEDULED)
                        .bestOf(profile.defaultBestOf())
                        .build();
                round.getMatches().add(match);
                roundMatches.add(match);
            }
            matchesByRound.add(roundMatches);
        }

        List<Match> firstRound = matchesByRound.get(0);
        for (int index = 0; index < firstRound.size(); index++) {
            Match match = firstRound.get(index);
            int participantIndex = index * 2;

            if (participantIndex < seeded.size()) {
                match.setRegistration1(seeded.get(participantIndex));
            }
            if (participantIndex + 1 < seeded.size()) {
                match.setRegistration2(seeded.get(participantIndex + 1));
            }
        }

        for (int roundIndex = 0; roundIndex < matchesByRound.size() - 1; roundIndex++) {
            List<Match> currentRound = matchesByRound.get(roundIndex);
            List<Match> nextRound = matchesByRound.get(roundIndex + 1);

            for (int matchIndex = 0; matchIndex < currentRound.size(); matchIndex++) {
                currentRound.get(matchIndex).setNextMatch(nextRound.get(matchIndex / 2));
            }
        }

        autoAdvanceByes(matchesByRound);

        return new BracketResult(
                rounds,
                matchesByRound.stream().flatMap(List::stream).toList()
        );
    }

    @Override
    public boolean isComplete(Bracket bracket, TournamentFormatProfile profile) {
        return bracket.getRounds().stream()
                .allMatch(round -> round.getStatus() == RoundStatus.COMPLETED);
    }

    private void autoAdvanceByes(List<List<Match>> matchesByRound) {
        boolean changed;
        do {
            changed = false;
            for (List<Match> roundMatches : matchesByRound) {
                for (Match match : roundMatches) {
                    if (match.getWinner() != null) {
                        continue;
                    }

                    if (match.getRegistration1() == null && match.getRegistration2() == null) {
                        match.setStatus(MatchStatus.BYE);
                        continue;
                    }

                    if (match.isBye()) {
                        match.setStatus(MatchStatus.BYE);
                        match.setWinner(match.getByeWinner());
                        changed |= placeWinner(match, match.getWinner());
                    }
                }
            }
        } while (changed);

        matchesByRound.stream()
                .flatMap(List::stream)
                .filter(match -> match.getStatus() == MatchStatus.BYE && match.getWinner() != null)
                .sorted(Comparator.comparing(Match::getPosition))
                .forEach(match -> placeWinner(match, match.getWinner()));
    }

    private boolean placeWinner(Match source, Registration winner) {
        if (source.getNextMatch() == null || winner == null) {
            return false;
        }

        Match nextMatch = source.getNextMatch();
        if (nextMatch.getRegistration1() == null) {
            nextMatch.setRegistration1(winner);
            return true;
        }
        if (nextMatch.getRegistration2() == null && !nextMatch.getRegistration1().getId().equals(winner.getId())) {
            nextMatch.setRegistration2(winner);
            return true;
        }
        return false;
    }

    private int nextPowerOfTwo(int value) {
        int power = 1;
        while (power < value) {
            power <<= 1;
        }
        return power;
    }

    private String resolveRoundName(int totalRounds, int currentRound) {
        int remaining = totalRounds - currentRound + 1;
        return switch (remaining) {
            case 1 -> "Final";
            case 2 -> "Semifinal";
            case 3 -> "Cuartos de final";
            default -> "Ronda " + currentRound;
        };
    }
}
