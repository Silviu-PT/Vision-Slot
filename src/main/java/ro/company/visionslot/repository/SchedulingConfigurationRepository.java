package ro.company.visionslot.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.company.visionslot.entity.SchedulingConfiguration;

public interface SchedulingConfigurationRepository extends JpaRepository<SchedulingConfiguration, Long> {

    Optional<SchedulingConfiguration> findTopByOrderByIdDesc();
}
