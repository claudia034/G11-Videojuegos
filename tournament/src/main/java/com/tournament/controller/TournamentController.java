package com.tournament.controller;

import com.tournament.dto.CreateTournamentRequest;
import com.tournament.dto.TournamentResponse;
import com.tournament.dto.UpdateTournamentRequest;
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
    public ResponseEntity<TournamentResponse> create(@Valid @RequestBody CreateTournamentRequest request) {
        TournamentResponse response = tournamentService.create(request);
        return ResponseEntity
                .created(URI.create("/tournaments/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TournamentResponse>> findAll() {
        return ResponseEntity.ok(tournamentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TournamentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTournamentRequest request
    ) {
        return ResponseEntity.ok(tournamentService.update(id, request));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<TournamentResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.publish(id));
    }

    @PostMapping("/{id}/rounds")
    public ResponseEntity<TournamentResponse> generateRounds(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.generateRounds(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        tournamentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
