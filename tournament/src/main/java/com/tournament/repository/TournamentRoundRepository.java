package com.tournament.repository;

import com.tournament.domain.entity.TournamentRound;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRoundRepository extends JpaRepository<TournamentRound, Long> {

    List<TournamentRound> findByTournamentIdOrderByRoundNumberAsc(Long tournamentId);
}
