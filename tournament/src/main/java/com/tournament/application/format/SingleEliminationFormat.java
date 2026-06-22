package com.tournament.application.format;

import com.tournament.domain.entity.*;
import com.tournament.domain.enums.MatchStatus;
import com.tournament.domain.enums.RoundStatus;
import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentRoundStatus;

import java.util.ArrayList;
import java.util.List;

public class SingleEliminationFormat implements TournamentFormatStrategy{

    @Override
    public TournamentFormat getFormat() {
        return TournamentFormat.SINGLE_ELIMINATION;
    }

    @Override
    public List<TournamentRound> generateRounds(Tournament tournament) {
        int size = nextPowerOfTwo(tournament.getMaxParticipants());
        int totalRounds = (int) (Math.log(size) / Math.log(2));

        List<TournamentRound> rounds = new ArrayList<>();

        for (int r = 1; r <= totalRounds; r++) {
            rounds.add(TournamentRound.builder()
                    .roundNumber(r)
                    .name(getRoundName(totalRounds, r))
                    .status(TournamentRoundStatus.PENDING)
                    .build());
        }
        return rounds;
    }

    @Override
    public int getMinimumParticipants() {
        return 2;
    }

    @Override
    public boolean isComplete(Bracket bracket) {
        return bracket.isComplete();
    }

    @Override
    public BracketResult generateBracket(List<Registration> seeded, Bracket bracket) {
        List<Round> rounds = new ArrayList<>();
        List<Match> matches = new ArrayList<>();

        int size = nextPowerOfTwo(seeded.size());
        int totalRounds = (int) (Math.log(size) / Math.log(2));

        for (int r = 0; r < totalRounds; r++) {
            int matchCount = size >> (r + 1);
            Round round = Round.builder()
                    .bracket(bracket)
                    .roundNumber(r + 1)
                    .name(getRoundName(totalRounds, r + 1))
                    .status(RoundStatus.PENDING)
                    .build();
            rounds.add(round);

            for (int m = 0; m < matchCount; m++) {
                Match match = Match.builder()
                        .round(round)
                        .position(m + 1)
                        .status(MatchStatus.SCHEDULED)
                        .bestOf(1)
                        .build();

                if (r == 0) {
                    int p1Index = m * 2;
                    int p2Index = m * 2 + 1;
                    if (p1Index < seeded.size()) match.setRegistration1(seeded.get(p1Index));
                    if (p2Index < seeded.size()) match.setRegistration2(seeded.get(p2Index));
                }
                matches.add(match);
            }
        }

        return new BracketResult(rounds, matches);
    }

    private int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) p <<= 1;
        return p;
    }

    private String getRoundName(int total, int current) {
        int remaining = total - current + 1;
        return switch (remaining) {
            case 1 -> "Final";
            case 2 -> "Semifinal";
            case 3 -> "Cuartos de Final";
            default -> "Ronda " + current;
        };
    }
}
