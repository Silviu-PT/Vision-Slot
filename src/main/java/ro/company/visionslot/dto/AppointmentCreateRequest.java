package ro.company.visionslot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import ro.company.visionslot.entity.enums.BookingSource;
import ro.company.visionslot.entity.enums.ParticipantType;

public record AppointmentCreateRequest(
        @NotBlank(message = "Marca este obligatorie.")
        String employeeCode,
        @NotBlank(message = "Numele este obligatoriu.")
        String fullName,
        @Email(message = "Adresa de e-mail nu este valida.")
        String email,
        @NotNull(message = "Tipul participantului este obligatoriu.")
        ParticipantType participantType,
        @NotNull(message = "Sursa programarii este obligatorie.")
        BookingSource bookingSource,
        @NotNull(message = "Data si ora programarii sunt obligatorii.")
        LocalDateTime appointmentStart
) {
}
