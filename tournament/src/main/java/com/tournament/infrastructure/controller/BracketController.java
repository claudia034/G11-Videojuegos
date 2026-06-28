package com.tournament.infrastructure.controller;

import com.tournament.application.dto.request.GenerateBracketRequest;
import com.tournament.application.dto.request.ScheduleRoundRequest;
import com.tournament.application.dto.response.*;
import com.tournament.application.service.BracketService;
import com.tournament.infrastructure.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tournaments")
@RequiredArgsConstructor
public class BracketController {

    private final BracketService bracketService;

    @PostMapping("/{tournamentId}/brackets/generate")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BracketResponse>> generate(
            @PathVariable Long tournamentId,
            @Valid @RequestBody GenerateBracketRequest request) {

        BracketResponse response = bracketService.generateBracket(tournamentId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Bracket generado exitosamente"));
    }

    @GetMapping("/{tournamentId}/brackets")
    public ResponseEntity<ApiResponse<BracketResponse>> getBracket(
            @PathVariable Long tournamentId) {

        return ResponseEntity.ok(
                ApiResponse.success(bracketService.getBracket(tournamentId)));
    }

    @PatchMapping("/rounds/{roundId}/schedule")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RoundResponse>> scheduleRound(
            @PathVariable Long roundId,
            @Valid @RequestBody ScheduleRoundRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        bracketService.scheduleRound(roundId, request),
                        "Ronda programada exitosamente"));
    }

    @GetMapping("/{tournamentId}/brackets/view")
    public ResponseEntity<ApiResponse<BracketViewDto>> getBracketView(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(ApiResponse.success(bracketService.getBracketView(tournamentId)));
    }
}
