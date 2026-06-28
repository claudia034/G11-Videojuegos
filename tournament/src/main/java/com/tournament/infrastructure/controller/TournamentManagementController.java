package com.tournament.infrastructure.controller;

import com.tournament.application.dto.response.MatchDetailResponse;
import com.tournament.application.dto.response.TournamentManagementSummaryDto;
import com.tournament.application.service.MatchService;
import com.tournament.application.service.TournamentManagementService;
import com.tournament.domain.entity.Match;
import com.tournament.domain.enums.MatchStatus;
import com.tournament.domain.repository.MatchRepository;
import com.tournament.infrastructure.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TournamentManagementController {

    private final TournamentManagementService tournamentManagementService;
    private final MatchRepository matchRepository;
    private final MatchService matchService;

    @GetMapping("/tournaments/{tournamentId}/management")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TournamentManagementSummaryDto>> getSummary(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(ApiResponse.success(tournamentManagementService.getSummary(tournamentId)));
    }

    @GetMapping("/tournaments/{tournamentId}/matches")
    public ResponseEntity<ApiResponse<List<MatchDetailResponse>>> getMatches(
            @PathVariable Long tournamentId,
            @RequestParam(required = false) MatchStatus status
    ) {
        List<Match> matches = status == null
                ? matchRepository.findAllByTournamentId(tournamentId)
                : matchRepository.findByTournamentIdAndStatus(tournamentId, status);

        List<MatchDetailResponse> response = matches.stream()
                .map(match -> matchService.getMatch(match.getId()))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
