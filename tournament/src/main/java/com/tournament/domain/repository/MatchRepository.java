package com.tournament.domain.repository;

import com.tournament.domain.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByRoundIdOrderByPositionAsc(Long roundId);

    @Query("SELECT m FROM Match m " +
            "WHERE m.round.bracket.tournament.id = :tid " +
            "ORDER BY m.round.roundNumber ASC, m.position ASC")
    List<Match> findAllByTournamentId(@Param("tid") Long tournamentId);
}
