package com.tournament.domain.repository;

import com.tournament.domain.entity.Match;
import com.tournament.domain.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByRoundIdOrderByPositionAsc(Long roundId);

    List<Match> findByStatusOrderByUpdatedAtDesc(MatchStatus status);

    @Query("SELECT m FROM Match m " +
            "WHERE m.round.bracket.tournament.id = :tid " +
            "ORDER BY m.round.roundNumber ASC, m.position ASC")
    List<Match> findAllByTournamentId(@Param("tid") Long tournamentId);

    @Query("""
            SELECT m FROM Match m
            WHERE m.round.bracket.tournament.id = :tournamentId
            AND m.status = :status
            ORDER BY m.round.roundNumber ASC, m.position ASC
            """)
    List<Match> findByTournamentIdAndStatus(
            @Param("tournamentId") Long tournamentId,
            @Param("status") MatchStatus status
    );
    @Query("""
            SELECT DISTINCT m FROM Match m
            LEFT JOIN m.registration1 r1 LEFT JOIN r1.player p1 LEFT JOIN r1.team t1 LEFT JOIN t1.members tm1
            LEFT JOIN m.registration2 r2 LEFT JOIN r2.player p2 LEFT JOIN r2.team t2 LEFT JOIN t2.members tm2
            WHERE m.status = 'COMPLETED'
            AND (p1.id = :playerId OR p2.id = :playerId OR tm1.player.id = :playerId OR tm2.player.id = :playerId)
            AND (:gameName IS NULL OR m.round.bracket.tournament.gameName = :gameName)
            AND (:tournamentName IS NULL OR m.round.bracket.tournament.name = :tournamentName)
            """)
    List<Match> findCompletedMatchesByPlayerAndFilters(
            @Param("playerId") Long playerId, 
            @Param("gameName") String gameName, 
            @Param("tournamentName") String tournamentName
    );
}
