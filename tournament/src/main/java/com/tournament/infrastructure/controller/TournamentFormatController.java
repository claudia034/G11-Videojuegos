package com.tournament.infrastructure.controller;

import com.tournament.application.dto.response.TournamentFormatOptionDto;
import com.tournament.application.service.TournamentFormatCatalogService;
import com.tournament.infrastructure.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tournament-formats")
@RequiredArgsConstructor
public class TournamentFormatController {

    private final TournamentFormatCatalogService tournamentFormatCatalogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TournamentFormatOptionDto>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(tournamentFormatCatalogService.getAvailableFormats()));
    }
}
