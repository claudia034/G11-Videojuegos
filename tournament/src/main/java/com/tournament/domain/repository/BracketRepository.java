package domain.repository;

import domain.entity.Bracket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BracketRepository extends JpaRepository<Bracket, Long> {
    Optional<Bracket> findByTournamentId(Long tournamentId);
    boolean existsByTournamentId(Long tournamentId);
}
