package ro.company.visionslot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.company.visionslot.entity.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}
