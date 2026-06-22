package com.tournament.application.format;

import com.tournament.domain.enums.TournamentFormat;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FormatFactory {

    private final Map<TournamentFormat, TournamentFormatStrategy> strategies;

    public FormatFactory(List<TournamentFormatStrategy> strategyList) {
        this.strategies = new EnumMap<>(TournamentFormat.class);
        strategyList.forEach(strategy -> strategies.put(strategy.getFormat(), strategy));
    }

    public TournamentFormatStrategy getFormat(@NotNull TournamentFormat format) {
        TournamentFormatStrategy strategy = strategies.get(format);
        if (strategy == null) {
            throw new IllegalArgumentException("Formato de torneo no soportado: " + format);
        }
        return strategy;
    }
}