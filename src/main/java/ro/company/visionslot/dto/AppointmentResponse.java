package ro.company.visionslot.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import ro.company.visionslot.entity.enums.AppointmentStatus;
import ro.company.visionslot.entity.enums.BookingSource;
import ro.company.visionslot.entity.enums.ParticipantType;

public record AppointmentResponse(
        UUID id,
        String employeeCode,
        String fullName,
        String email,
        ParticipantType participantType,
        BookingSource bookingSource,
        AppointmentStatus status,
        LocalDateTime appointmentStart,
        LocalDateTime appointmentEnd,
        Instant createdAt,
        Instant updatedAt
) {
}
