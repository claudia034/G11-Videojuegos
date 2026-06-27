package com.tournament.domain.repository;

import com.tournament.application.dto.response.PlayerRankingDto;
import com.tournament.domain.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query("SELECT new com.tournament.application.dto.response.PlayerRankingDto(" +
            "p.id, p.username, p.eloRating, ps.wins, ps.losses, ps.tournamentsPlayed) " +
            "FROM Player p JOIN p.playerStats ps " +
            "ORDER BY p.eloRating DESC")
    Page<PlayerRankingDto> findRankingProjection(Pageable pageable);

}
