package com.tournament.application.format;

import com.tournament.domain.enums.FormatType;
import org.springframework.stereotype.Component;

@Component
public class FormatFactory {

    public TournamentFormat getFormat(FormatType type) {
        return switch (type) {
            case SINGLE_ELIMINATION -> new SingleEliminationFormat();
            case DOUBLE_ELIMINATION -> new SingleEliminationFormat();
            case ROUND_ROBIN        -> new SingleEliminationFormat();
        };
    }
}
