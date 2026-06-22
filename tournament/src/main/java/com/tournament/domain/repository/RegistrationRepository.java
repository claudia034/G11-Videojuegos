package com.tournament.domain.repository;

import com.tournament.domain.entity.Registration;
import com.tournament.domain.enums.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    boolean existsByTournamentIdAndUserId(Long tournamentId, Long userId);

    boolean existsByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    int countByTournamentIdAndStatusNot(Long tournamentId, RegistrationStatus status);

    Page<Registration> findByTournamentId(Long tournamentId, Pageable pageable);
}