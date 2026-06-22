package com.tournament.infrastructure.controller;

import com.tournament.application.dto.request.RegisterRequest;
import com.tournament.application.dto.response.RegistrationResponse;
import com.tournament.application.service.RegistrationService;
import com.tournament.domain.entity.User;
import com.tournament.infrastructure.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/tournaments/{tournamentId}/register")
    @PreAuthorize("hasAnyRole('PLAYER', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(
            @PathVariable Long tournamentId,
            @Valid @RequestBody RegisterRequest request) {

        RegistrationResponse response = registrationService.register(tournamentId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Inscripción realizada exitosamente"));
    }

    @GetMapping("/tournaments/{tournamentId}/participants")
    public ResponseEntity<ApiResponse<Page<RegistrationResponse>>> getParticipants(
            @PathVariable Long tournamentId,
            @PageableDefault(size = 20, sort = "registeredAt") Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(registrationService.getParticipants(tournamentId, pageable)));
    }

    @DeleteMapping("/registrations/{registrationId}")
    @PreAuthorize("hasAnyRole('PLAYER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> withdraw(
            @PathVariable Long registrationId,
            @AuthenticationPrincipal User currentUser) {

        RegistrationResponse response = registrationService.withdraw(registrationId, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success(response, "Inscripción retirada exitosamente"));
    }
}
