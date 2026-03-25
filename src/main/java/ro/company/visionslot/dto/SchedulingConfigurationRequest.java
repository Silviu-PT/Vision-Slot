package ro.company.visionslot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record SchedulingConfigurationRequest(
        @NotEmpty(message = "Trebuie selectata cel putin o zi de consultatie.")
        Set<DayOfWeek> consultationDays,
        @NotNull(message = "Ora de inceput este obligatorie.")
        LocalTime consultationStartTime,
        @NotNull(message = "Ora de final este obligatorie.")
        LocalTime consultationEndTime,
        LocalTime lunchBreakStart,
        LocalTime lunchBreakEnd,
        @NotNull(message = "Durata slotului este obligatorie.")
        @Positive(message = "Durata slotului trebuie sa fie mai mare ca 0.")
        Integer slotDurationMinutes,
        @NotNull(message = "Data de inceput a ferestrei de programare este obligatorie.")
        LocalDate bookingWindowStart,
        @NotNull(message = "Data de sfarsit a ferestrei de programare este obligatorie.")
        LocalDate bookingWindowEnd
) {
}
