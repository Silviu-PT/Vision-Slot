package ro.company.visionslot.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AppointmentRescheduleRequest(
        @NotNull(message = "Noua data si ora sunt obligatorii.")
        LocalDateTime newAppointmentStart
) {
}
