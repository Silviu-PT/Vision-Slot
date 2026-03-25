package ro.company.visionslot.dto;

import java.util.List;

public record ConfirmationResponse(
        String message,
        AppointmentResponse appointment,
        List<NotificationResponse> notifications
) {
}
