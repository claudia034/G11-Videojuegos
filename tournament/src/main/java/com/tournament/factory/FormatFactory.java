package com.tournament.factory;

import com.tournament.domain.enums.TournamentFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FormatFactory {

    private final Map<TournamentFormat, com.tournament.strategy.TournamentFormat> strategies;

    public FormatFactory(List<com.tournament.strategy.TournamentFormat> strategyList) {
        this.strategies = new EnumMap<>(TournamentFormat.class);
        strategyList.forEach(strategy -> strategies.put(strategy.getFormat(), strategy));
    }

    public com.tournament.strategy.TournamentFormat getStrategy(TournamentFormat format) {
        com.tournament.strategy.TournamentFormat strategy = strategies.get(format);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported tournament format: " + format);
        }
        return strategy;
    }
}
