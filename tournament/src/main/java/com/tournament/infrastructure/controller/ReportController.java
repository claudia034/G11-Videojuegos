package com.tournament.infrastructure.controller;

import com.tournament.application.dto.response.PopularGameReportDto;
import com.tournament.application.dto.response.PopularTournamentByGameDto;
import com.tournament.application.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/popular-games")
    public ResponseEntity<List<PopularGameReportDto>> getPopularGames() {
        return ResponseEntity.ok(reportService.getPopularGamesReport());
    }

    @GetMapping("/popular-tournaments-by-game")
    public ResponseEntity<List<PopularTournamentByGameDto>> getPopularTournamentsByGame() {
        return ResponseEntity.ok(reportService.getPopularTournamentsByGameReport());
    }
}
