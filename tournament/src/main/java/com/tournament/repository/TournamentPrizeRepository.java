package com.tournament.repository;

import com.tournament.domain.entity.TournamentPrize;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentPrizeRepository extends JpaRepository<TournamentPrize, Long> {

    List<TournamentPrize> findByTournamentIdOrderByPositionAsc(Long tournamentId);
}
