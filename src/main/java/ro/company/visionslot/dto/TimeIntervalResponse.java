package ro.company.visionslot.dto;

import java.time.LocalDateTime;

public record TimeIntervalResponse(LocalDateTime start, LocalDateTime end) {
}
