package domain.repository;

import domain.entity.RegistrationRoster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRosterRepository extends JpaRepository<RegistrationRoster, Long> {
    List<RegistrationRoster> findByRegistration(Long registrationId);
}
