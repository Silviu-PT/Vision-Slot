package ro.proiectcolectiv.ophthalmology.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentRequest(@NotNull Long newSlotId) {
}
