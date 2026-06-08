package com.tournament.service;

import com.tournament.dto.CreateTournamentRequest;
import com.tournament.dto.TournamentResponse;
import com.tournament.dto.UpdateTournamentRequest;
import java.util.List;

public interface TournamentService {

    TournamentResponse create(CreateTournamentRequest request);

    List<TournamentResponse> findAll();

    TournamentResponse findById(Long id);

    TournamentResponse update(Long id, UpdateTournamentRequest request);

    TournamentResponse publish(Long id);

    TournamentResponse generateRounds(Long id);

    void deleteById(Long id);
}
