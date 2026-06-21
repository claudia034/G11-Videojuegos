package com.tournament.domain.repository;

import com.tournament.domain.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Page<Player> findAllByOrderByEloRatingDesc(Pageable pageable);
}
