package com.tournament.domain.repository;

import com.tournament.domain.repository.projection.PlayerStatsProjection;
import com.tournament.domain.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query("SELECT p.id as playerId, p.username as username, p.eloRating as eloRating, " +
           "COALESCE(ps.wins, 0) as wins, COALESCE(ps.losses, 0) as losses, " +
           "COALESCE(ps.tournamentsPlayed, 0) as tournamentsPlayed, " +
           "COALESCE(ps.virtualPoints, 0) as virtualPoints " +
           "FROM Player p LEFT JOIN p.playerStats ps " +
           "ORDER BY p.eloRating DESC")
    Page<PlayerStatsProjection> findGlobalRanking(Pageable pageable);

    @Query("SELECT p.id as playerId, p.username as username, p.eloRating as eloRating, " +
           "COALESCE(ps.wins, 0) as wins, COALESCE(ps.losses, 0) as losses, " +
           "COALESCE(ps.tournamentsPlayed, 0) as tournamentsPlayed, " +
           "COALESCE(ps.virtualPoints, 0) as virtualPoints " +
           "FROM Player p LEFT JOIN p.playerStats ps " +
           "WHERE p.id = :playerId")
    Optional<PlayerStatsProjection> findPlayerStatsProjectionById(Long playerId);
    Optional<Player> findByUserId(Long userId);
}
