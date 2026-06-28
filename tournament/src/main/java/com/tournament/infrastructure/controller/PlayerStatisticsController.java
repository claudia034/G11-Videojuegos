package com.tournament.infrastructure.controller;

import com.tournament.application.dto.response.PlayerHistoryDto;
import com.tournament.application.dto.response.PlayerRankingDto;
import com.tournament.application.dto.response.PlayerStatsDto;
import com.tournament.application.service.PlayerStatisticsService;
import com.tournament.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tournament.application.dto.response.PrivatePlayerStatsDto;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerStatisticsController {

    private final PlayerStatisticsService playerStatisticsService;

    @GetMapping("/ranking")
    public ResponseEntity<Page<PlayerRankingDto>> getGlobalRanking(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(playerStatisticsService.getGlobalRanking(pageable));
    }

    @GetMapping("/me/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrivatePlayerStatsDto> getCurrentPlayerStats(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(playerStatisticsService.getCurrentPlayerStats(currentUser.getId()));
    }

    @GetMapping("/{playerId}/stats")
    public ResponseEntity<PlayerStatsDto> getPlayerStats(@PathVariable Long playerId) {
        return ResponseEntity.ok(playerStatisticsService.getPlayerStats(playerId));
    }

    @GetMapping("/{playerId}/history")
    public ResponseEntity<PlayerHistoryDto> getPlayerHistory(
            @PathVariable Long playerId,
            @RequestParam(required = false) String gameName,
            @RequestParam(required = false) String tournamentName) {
        return ResponseEntity.ok(playerStatisticsService.getPlayerHistory(playerId, gameName, tournamentName));
    }
}
