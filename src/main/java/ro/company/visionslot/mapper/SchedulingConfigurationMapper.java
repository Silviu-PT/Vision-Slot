package ro.company.visionslot.mapper;

import java.util.Comparator;
import ro.company.visionslot.dto.SchedulingConfigurationResponse;
import ro.company.visionslot.entity.SchedulingConfiguration;

public final class SchedulingConfigurationMapper {

    private SchedulingConfigurationMapper() {
    }

    public static SchedulingConfigurationResponse toResponse(SchedulingConfiguration configuration) {
        return new SchedulingConfigurationResponse(
                configuration.getId(),
                configuration.getConsultationDays().stream().sorted(Comparator.naturalOrder()).toList(),
                configuration.getConsultationStartTime(),
                configuration.getConsultationEndTime(),
                configuration.getLunchBreakStart(),
                configuration.getLunchBreakEnd(),
                configuration.getSlotDurationMinutes(),
                configuration.getBookingWindowStart(),
                configuration.getBookingWindowEnd(),
                configuration.getUpdatedAt()
        );
    }
}
