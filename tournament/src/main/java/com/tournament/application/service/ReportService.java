package com.tournament.application.service;

import com.tournament.application.dto.response.PopularGameReportDto;
import com.tournament.application.dto.response.PopularTournamentByGameDto;
import com.tournament.domain.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TournamentRepository tournamentRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "popularGames")
    public List<PopularGameReportDto> getPopularGamesReport() {
        return tournamentRepository.findPopularGames();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "popularTournamentsByGame")
    public List<PopularTournamentByGameDto> getPopularTournamentsByGameReport() {
        return tournamentRepository.findTournamentPopularityRows().stream()
                .collect(java.util.stream.Collectors.toMap(
                        PopularTournamentByGameDto::getGameName,
                        Function.identity(),
                        (current, candidate) -> candidate.getParticipantCount() > current.getParticipantCount()
                                ? candidate
                                : current,
                        java.util.LinkedHashMap::new
                ))
                .values()
                .stream()
                .sorted(java.util.Comparator
                        .comparing(PopularTournamentByGameDto::getParticipantCount, java.util.Comparator.reverseOrder())
                        .thenComparing(PopularTournamentByGameDto::getGameName))
                .toList();
    }
}
