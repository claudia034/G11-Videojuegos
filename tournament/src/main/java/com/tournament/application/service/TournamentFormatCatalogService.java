package com.tournament.application.service;

import com.tournament.application.dto.response.TournamentFormatOptionDto;
import com.tournament.application.format.FormatFactory;
import com.tournament.domain.enums.TournamentFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TournamentFormatCatalogService {

    private static final Set<TournamentFormat> ACTIVE_FORMATS = EnumSet.of(
            TournamentFormat.SINGLE_ELIMINATION,
            TournamentFormat.DOUBLE_ELIMINATION,
            TournamentFormat.ROUND_ROBIN,
            TournamentFormat.SWISS
    );

    private final FormatFactory formatFactory;

    @Transactional(readOnly = true)
    public List<TournamentFormatOptionDto> getAvailableFormats() {
        return formatFactory.getAllFormats().stream()
                .filter(strategy -> ACTIVE_FORMATS.contains(strategy.getProfile().format()))
                .map(strategy -> TournamentFormatOptionDto.from(strategy.getProfile()))
                .toList();
    }
}
