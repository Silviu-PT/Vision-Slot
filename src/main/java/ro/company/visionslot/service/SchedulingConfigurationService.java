package ro.company.visionslot.service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.company.visionslot.dto.SchedulingConfigurationRequest;
import ro.company.visionslot.dto.SchedulingConfigurationResponse;
import ro.company.visionslot.entity.SchedulingConfiguration;
import ro.company.visionslot.exception.BusinessRuleException;
import ro.company.visionslot.exception.ResourceNotFoundException;
import ro.company.visionslot.mapper.SchedulingConfigurationMapper;
import ro.company.visionslot.repository.SchedulingConfigurationRepository;

@Service
public class SchedulingConfigurationService {

    private final SchedulingConfigurationRepository schedulingConfigurationRepository;

    public SchedulingConfigurationService(SchedulingConfigurationRepository schedulingConfigurationRepository) {
        this.schedulingConfigurationRepository = schedulingConfigurationRepository;
    }

    @Transactional(readOnly = true)
    public SchedulingConfigurationResponse getActiveConfiguration() {
        return SchedulingConfigurationMapper.toResponse(getRequiredConfiguration());
    }

    @Transactional(readOnly = true)
    public SchedulingConfiguration getRequiredConfiguration() {
        return schedulingConfigurationRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new ResourceNotFoundException("Nu exista inca o configurare activa."));
    }

    @Transactional
    public SchedulingConfigurationResponse saveConfiguration(SchedulingConfigurationRequest request) {
        validateRequest(request);
        SchedulingConfiguration configuration = schedulingConfigurationRepository.findTopByOrderByIdDesc()
                .orElseGet(SchedulingConfiguration::new);
        configuration.setConsultationDays(new LinkedHashSet<>(request.consultationDays()));
        configuration.setConsultationStartTime(request.consultationStartTime());
        configuration.setConsultationEndTime(request.consultationEndTime());
        configuration.setLunchBreakStart(request.lunchBreakStart());
        configuration.setLunchBreakEnd(request.lunchBreakEnd());
        configuration.setSlotDurationMinutes(request.slotDurationMinutes());
        configuration.setBookingWindowStart(request.bookingWindowStart());
        configuration.setBookingWindowEnd(request.bookingWindowEnd());
        return SchedulingConfigurationMapper.toResponse(schedulingConfigurationRepository.save(configuration));
    }

    private void validateRequest(SchedulingConfigurationRequest request) {
        if (!request.consultationStartTime().isBefore(request.consultationEndTime())) {
            throw new BusinessRuleException("Intervalul consultatiilor este invalid.");
        }
        if (request.bookingWindowStart().isAfter(request.bookingWindowEnd())) {
            throw new BusinessRuleException("Fereastra de programare este invalida.");
        }
        validateLunchBreak(
                request.consultationStartTime(),
                request.consultationEndTime(),
                request.lunchBreakStart(),
                request.lunchBreakEnd()
        );

        long totalMinutes = Duration.between(request.consultationStartTime(), request.consultationEndTime()).toMinutes();
        if (request.slotDurationMinutes() > totalMinutes) {
            throw new BusinessRuleException("Durata slotului depaseste programul de consultatii.");
        }
    }

    private void validateLunchBreak(
            LocalTime consultationStart,
            LocalTime consultationEnd,
            LocalTime lunchBreakStart,
            LocalTime lunchBreakEnd
    ) {
        boolean onlyOneBreakBoundaryProvided = lunchBreakStart == null ^ lunchBreakEnd == null;
        if (onlyOneBreakBoundaryProvided) {
            throw new BusinessRuleException("Pauza de masa trebuie definita complet sau eliminata.");
        }
        if (lunchBreakStart == null) {
            return;
        }
        if (!lunchBreakStart.isBefore(lunchBreakEnd)) {
            throw new BusinessRuleException("Intervalul pauzei de masa este invalid.");
        }
        if (lunchBreakStart.isBefore(consultationStart) || lunchBreakEnd.isAfter(consultationEnd)) {
            throw new BusinessRuleException("Pauza de masa trebuie sa fie inclusa in programul consultatiilor.");
        }
    }
}
