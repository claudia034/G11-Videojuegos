package com.tournament.domain.repository;

import com.tournament.application.dto.response.PopularGameReportDto;
import com.tournament.domain.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    
    @Query("SELECT new com.tournament.application.dto.response.PopularGameReportDto(t.gameName, COUNT(t)) FROM Tournament t GROUP BY t.gameName ORDER BY COUNT(t) DESC")
    List<PopularGameReportDto> findPopularGames();
}
