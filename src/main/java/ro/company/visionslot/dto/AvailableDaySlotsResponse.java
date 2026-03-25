package ro.company.visionslot.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record AvailableDaySlotsResponse(
        LocalDate date,
        DayOfWeek dayOfWeek,
        List<TimeIntervalResponse> slots
) {
}
