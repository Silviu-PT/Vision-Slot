package ro.proiectcolectiv.ophthalmology.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        long appointmentId,
        long campaignId,
        long slotId,
        String employeeNumber,
        String fullName,
        String email,
        String status,
        LocalDateTime slotStart,
        LocalDateTime slotEnd,
        UUID cancelToken) {
}
