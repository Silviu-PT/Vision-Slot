package ro.proiectcolectiv.ophthalmology.model;

import java.time.LocalDateTime;

public record AppointmentExportRow(
        long appointmentId,
        LocalDateTime slotStart,
        LocalDateTime slotEnd,
        String employeeNumber,
        String fullName,
        String email) {
}
