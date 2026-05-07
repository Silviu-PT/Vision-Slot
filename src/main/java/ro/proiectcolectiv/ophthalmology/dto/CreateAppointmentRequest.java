package ro.proiectcolectiv.ophthalmology.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAppointmentRequest(
        @NotNull Long slotId,
        @NotBlank String employeeNumber,
        @NotBlank String fullName,
        @NotBlank @Email String email) {
}
