package com.tournament.infrastructure.controller;

import com.tournament.application.dto.request.AdminMatchDecisionRequest;
import com.tournament.application.dto.request.ResolveDisputeRequest;
import com.tournament.application.dto.request.ScheduleMatchRequest;
import com.tournament.application.dto.request.SubmitResultRequest;
import com.tournament.application.dto.response.MatchDetailResponse;
import com.tournament.application.service.MatchService;
import com.tournament.domain.entity.User;
import com.tournament.infrastructure.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/matches/{matchId}")
    public ResponseEntity<ApiResponse<MatchDetailResponse>> getMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(ApiResponse.success(matchService.getMatch(matchId)));
    }

    @PatchMapping("/matches/{matchId}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<MatchDetailResponse>> scheduleMatch(
            @PathVariable Long matchId,
            @Valid @RequestBody ScheduleMatchRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(matchService.scheduleMatch(matchId, request), "Partido programado"));
    }

    @PostMapping("/matches/{matchId}/admin-decision")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MatchDetailResponse>> decideMatchAsAdmin(
            @PathVariable Long matchId,
            @Valid @RequestBody AdminMatchDecisionRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        matchService.decideMatchAsAdmin(matchId, request, currentUser.getId()),
                        "Resultado validado por administracion"));
    }

    @PostMapping("/matches/{matchId}/start")
    @PreAuthorize("hasAnyRole('PLAYER', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<MatchDetailResponse>> startMatch(
            @PathVariable Long matchId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                ApiResponse.success(matchService.startMatch(matchId, currentUser.getId()), "Partido iniciado"));
    }

    @PostMapping("/matches/{matchId}/result")
    @PreAuthorize("hasAnyRole('PLAYER', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<MatchDetailResponse>> submitResult(
            @PathVariable Long matchId,
            @Valid @RequestBody SubmitResultRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                ApiResponse.success(matchService.submitResult(matchId, request, currentUser.getId()), "Resultado reportado"));
    }

    @PostMapping("/matches/{matchId}/confirm")
    @PreAuthorize("hasAnyRole('PLAYER', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<MatchDetailResponse>> confirmResult(
            @PathVariable Long matchId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                ApiResponse.success(matchService.confirmResult(matchId, currentUser.getId()), "Resultado confirmado"));
    }

    @PostMapping("/matches/{matchId}/dispute")
    @PreAuthorize("hasAnyRole('PLAYER', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<MatchDetailResponse>> disputeResult(
            @PathVariable Long matchId,
            @RequestParam String reason,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                ApiResponse.success(matchService.disputeResult(matchId, reason, currentUser.getId()), "Disputa registrada"));
    }

    @PutMapping("/disputes/{matchId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MatchDetailResponse>> resolveDispute(
            @PathVariable Long matchId,
            @Valid @RequestBody ResolveDisputeRequest request,
            @RequestParam Long adminUserId) {

        return ResponseEntity.ok(
                ApiResponse.success(matchService.resolveDispute(matchId, request, adminUserId), "Disputa resuelta"));
    }
}
