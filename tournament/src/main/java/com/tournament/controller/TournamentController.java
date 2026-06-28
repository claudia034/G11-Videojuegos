package com.tournament.controller;

import com.tournament.dto.CreateTournamentRequest;
import com.tournament.dto.TournamentResponse;
import com.tournament.dto.UpdateTournamentRequest;
import com.tournament.infrastructure.response.ApiResponse;
import com.tournament.service.TournamentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/tournaments", "/api/v1/tournaments"})
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    @PostMapping
    public ResponseEntity<ApiResponse<TournamentResponse>> create(@Valid @RequestBody CreateTournamentRequest request) {
        TournamentResponse response = tournamentService.create(request);
        return ResponseEntity
                .created(URI.create("/tournaments/" + response.id()))
                .body(ApiResponse.created(response, "Torneo creado exitosamente"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TournamentResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TournamentResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TournamentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTournamentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(tournamentService.update(id, request), "Torneo actualizado exitosamente")
        );
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<TournamentResponse>> publish(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.publish(id), "Torneo publicado exitosamente"));
    }

    @PostMapping("/{id}/close-registration")
    public ResponseEntity<ApiResponse<TournamentResponse>> closeRegistration(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(tournamentService.closeRegistration(id), "Inscripciones cerradas exitosamente")
        );
    }

    @PostMapping("/{id}/rounds")
    public ResponseEntity<ApiResponse<TournamentResponse>> generateRounds(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(tournamentService.generateRounds(id), "Rondas generadas exitosamente")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        tournamentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
