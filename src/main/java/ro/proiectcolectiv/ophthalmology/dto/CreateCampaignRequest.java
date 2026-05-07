package ro.proiectcolectiv.ophthalmology.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateCampaignRequest(
        @NotBlank String name,
        @NotNull LocalDate bookingStartDate,
        @NotNull LocalDate bookingEndDate,
        @NotEmpty List<@Valid ConsultationDayRequest> consultationDays) {
}
