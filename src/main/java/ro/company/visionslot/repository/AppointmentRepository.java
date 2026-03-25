package ro.company.visionslot.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.company.visionslot.entity.Appointment;
import ro.company.visionslot.entity.enums.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    boolean existsByEmployeeCodeAndStatus(String employeeCode, AppointmentStatus status);

    Optional<Appointment> findByEmployeeCodeAndStatus(String employeeCode, AppointmentStatus status);

    boolean existsByStatusAndAppointmentStartLessThanAndAppointmentEndGreaterThan(
            AppointmentStatus status,
            LocalDateTime appointmentEnd,
            LocalDateTime appointmentStart
    );

    boolean existsByStatusAndAppointmentStartLessThanAndAppointmentEndGreaterThanAndIdNot(
            AppointmentStatus status,
            LocalDateTime appointmentEnd,
            LocalDateTime appointmentStart,
            UUID id
    );

    List<Appointment> findByStatusAndAppointmentStartGreaterThanEqualAndAppointmentStartLessThanOrderByAppointmentStart(
            AppointmentStatus status,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}
