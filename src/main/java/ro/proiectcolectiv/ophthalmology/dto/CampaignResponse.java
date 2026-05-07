package ro.proiectcolectiv.ophthalmology.dto;

import java.time.LocalDate;

public record CampaignResponse(
        long campaignId,
        String name,
        LocalDate bookingStartDate,
        LocalDate bookingEndDate,
        String status,
        int consultationDayCount,
        int slotCount) {
}
