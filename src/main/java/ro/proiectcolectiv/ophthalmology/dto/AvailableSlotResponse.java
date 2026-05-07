package ro.proiectcolectiv.ophthalmology.dto;

import java.time.LocalDateTime;

public record AvailableSlotResponse(
        long slotId,
        LocalDateTime start,
        LocalDateTime end) {
}
