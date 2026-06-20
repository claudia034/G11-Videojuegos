package com.tournament.domain.repository;

import com.tournament.domain.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoundRepository extends JpaRepository<Round, Long> {
    List<Round> findByBracketIdOrderByRoundNumberAsc(Long bracketId);
}
