package ro.company.visionslot.dto;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record SchedulingConfigurationResponse(
        Long id,
        List<DayOfWeek> consultationDays,
        LocalTime consultationStartTime,
        LocalTime consultationEndTime,
        LocalTime lunchBreakStart,
        LocalTime lunchBreakEnd,
        Integer slotDurationMinutes,
        LocalDate bookingWindowStart,
        LocalDate bookingWindowEnd,
        Instant updatedAt
) {
}
