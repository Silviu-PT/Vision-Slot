package ro.company.visionslot.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import ro.company.visionslot.entity.enums.AppointmentStatus;
import ro.company.visionslot.entity.enums.BookingSource;
import ro.company.visionslot.entity.enums.ParticipantType;

public record AppointmentReportResponse(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String employeeCode,
        String fullName,
        String email,
        ParticipantType participantType,
        BookingSource bookingSource,
        AppointmentStatus status
) {
}
