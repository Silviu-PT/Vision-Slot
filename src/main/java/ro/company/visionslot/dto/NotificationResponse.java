package ro.company.visionslot.dto;

import ro.company.visionslot.entity.enums.NotificationChannel;

public record NotificationResponse(
        NotificationChannel channel,
        boolean delivered,
        String recipient,
        String message
) {
}
