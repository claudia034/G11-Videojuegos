package com.tournament.application.service;

import com.tournament.application.dto.response.PopularGameReportDto;
import com.tournament.domain.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TournamentRepository tournamentRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "popularGames")
    public List<PopularGameReportDto> getPopularGamesReport() {
        return tournamentRepository.findPopularGames();
    }
}
