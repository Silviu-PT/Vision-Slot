package ro.proiectcolectiv.ophthalmology.model;

import java.time.LocalDate;

public record Campaign(
        long id,
        String name,
        LocalDate bookingStartDate,
        LocalDate bookingEndDate,
        String status,
        int consultationDayCount,
        int slotCount) {
}
