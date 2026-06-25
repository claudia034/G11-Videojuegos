package com.tournament.application.format;

import com.tournament.domain.entity.Bracket;
import com.tournament.domain.entity.Registration;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.enums.TournamentFormat;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FormatFactory {

    private final Map<TournamentFormat, TournamentFormatStrategy> strategies;

    public FormatFactory(
            List<TournamentFormatStrategy> strategyList,
            List<com.tournament.strategy.TournamentFormat> legacyStrategyList
    ) {
        this.strategies = new EnumMap<>(TournamentFormat.class);
        strategyList.forEach(strategy -> strategies.put(strategy.getFormat(), strategy));
        legacyStrategyList.forEach(strategy ->
                strategies.putIfAbsent(strategy.getFormat(), new LegacyTournamentFormatAdapter(strategy)));
    }

    public TournamentFormatStrategy getFormat(@NotNull TournamentFormat format) {
        TournamentFormatStrategy strategy = strategies.get(format);
        if (strategy == null) {
            throw new IllegalArgumentException("Formato de torneo no soportado: " + format);
        }
        return strategy;
    }

    private record LegacyTournamentFormatAdapter(
            com.tournament.strategy.TournamentFormat delegate
    ) implements TournamentFormatStrategy {

        @Override
        public TournamentFormat getFormat() {
            return delegate.getFormat();
        }

        @Override
        public List<TournamentRound> generateRounds(Tournament tournament) {
            return delegate.generateRounds(tournament);
        }

        @Override
        public BracketResult generateBracket(
                List<Registration> seeded,
                Bracket bracket
        ) {
            throw new IllegalArgumentException("Bracket generation is not implemented for format: " + getFormat());
        }

        @Override
        public int getMinimumParticipants() {
            return 2;
        }

        @Override
        public boolean isComplete(Bracket bracket) {
            return bracket.isComplete();
        }
    }
}
