package ro.proiectcolectiv.ophthalmology.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConsultationDayRequest(
        @NotNull LocalDate consultationDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        LocalTime lunchStart,
        LocalTime lunchEnd,
        @NotNull @Min(1) @Max(240) Integer slotDurationMinutes) {
}
