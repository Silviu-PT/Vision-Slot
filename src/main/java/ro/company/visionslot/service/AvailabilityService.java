package ro.company.visionslot.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.company.visionslot.dto.AvailableDaySlotsResponse;
import ro.company.visionslot.dto.TimeIntervalResponse;
import ro.company.visionslot.entity.Appointment;
import ro.company.visionslot.entity.SchedulingConfiguration;
import ro.company.visionslot.entity.enums.AppointmentStatus;
import ro.company.visionslot.exception.BusinessRuleException;
import ro.company.visionslot.exception.ConflictException;
import ro.company.visionslot.repository.AppointmentRepository;

@Service
public class AvailabilityService {

    private final SchedulingConfigurationService schedulingConfigurationService;
    private final AppointmentRepository appointmentRepository;

    public AvailabilityService(
            SchedulingConfigurationService schedulingConfigurationService,
            AppointmentRepository appointmentRepository
    ) {
        this.schedulingConfigurationService = schedulingConfigurationService;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<AvailableDaySlotsResponse> getAvailableSlots(LocalDate startDate, LocalDate endDate) {
        SchedulingConfiguration configuration = schedulingConfigurationService.getRequiredConfiguration();
        LocalDate effectiveStart = startDate == null
                ? configuration.getBookingWindowStart()
                : max(startDate, configuration.getBookingWindowStart());
        LocalDate effectiveEnd = endDate == null
                ? configuration.getBookingWindowEnd()
                : min(endDate, configuration.getBookingWindowEnd());

        if (effectiveStart.isAfter(effectiveEnd)) {
            return List.of();
        }

        List<Appointment> activeAppointments = appointmentRepository
                .findByStatusAndAppointmentStartGreaterThanEqualAndAppointmentStartLessThanOrderByAppointmentStart(
                        AppointmentStatus.ACTIVE,
                        effectiveStart.atStartOfDay(),
                        effectiveEnd.plusDays(1).atStartOfDay()
                );

        Map<LocalDate, List<Appointment>> appointmentsByDate = new HashMap<>();
        for (Appointment appointment : activeAppointments) {
            appointmentsByDate.computeIfAbsent(appointment.getAppointmentStart().toLocalDate(), ignored -> new ArrayList<>())
                    .add(appointment);
        }

        List<AvailableDaySlotsResponse> result = new ArrayList<>();
        for (LocalDate current = effectiveStart; !current.isAfter(effectiveEnd); current = current.plusDays(1)) {
            if (!configuration.getConsultationDays().contains(current.getDayOfWeek())) {
                continue;
            }
            List<TimeIntervalResponse> slots = buildDailyAvailability(
                    current,
                    configuration,
                    appointmentsByDate.getOrDefault(current, List.of())
            );
            if (!slots.isEmpty()) {
                result.add(new AvailableDaySlotsResponse(current, current.getDayOfWeek(), slots));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public TimeIntervalResponse validateAndResolveSlot(LocalDateTime slotStart, UUID appointmentToIgnore) {
        SchedulingConfiguration configuration = schedulingConfigurationService.getRequiredConfiguration();
        validateAgainstConfiguration(slotStart, configuration);

        LocalDateTime slotEnd = slotStart.plusMinutes(configuration.getSlotDurationMinutes());
        boolean occupied = appointmentToIgnore == null
                ? appointmentRepository.existsByStatusAndAppointmentStartLessThanAndAppointmentEndGreaterThan(
                        AppointmentStatus.ACTIVE,
                        slotEnd,
                        slotStart
                )
                : appointmentRepository.existsByStatusAndAppointmentStartLessThanAndAppointmentEndGreaterThanAndIdNot(
                        AppointmentStatus.ACTIVE,
                        slotEnd,
                        slotStart,
                        appointmentToIgnore
                );

        if (occupied) {
            throw new ConflictException("Intervalul selectat nu mai este disponibil.");
        }
        return new TimeIntervalResponse(slotStart, slotEnd);
    }

    private List<TimeIntervalResponse> buildDailyAvailability(
            LocalDate date,
            SchedulingConfiguration configuration,
            List<Appointment> dailyAppointments
    ) {
        List<Appointment> sortedAppointments = dailyAppointments.stream()
                .sorted(Comparator.comparing(Appointment::getAppointmentStart))
                .toList();

        List<TimeIntervalResponse> slots = new ArrayList<>();
        LocalDateTime slotStart = date.atTime(configuration.getConsultationStartTime());
        LocalDateTime dayEnd = date.atTime(configuration.getConsultationEndTime());

        while (!slotStart.plusMinutes(configuration.getSlotDurationMinutes()).isAfter(dayEnd)) {
            LocalDateTime slotEnd = slotStart.plusMinutes(configuration.getSlotDurationMinutes());
            if (!overlapsLunchBreak(slotStart.toLocalTime(), slotEnd.toLocalTime(), configuration)
                    && isFree(slotStart, slotEnd, sortedAppointments)) {
                slots.add(new TimeIntervalResponse(slotStart, slotEnd));
            }
            slotStart = slotEnd;
        }
        return slots;
    }

    private void validateAgainstConfiguration(LocalDateTime slotStart, SchedulingConfiguration configuration) {
        LocalDate appointmentDate = slotStart.toLocalDate();
        DayOfWeek dayOfWeek = appointmentDate.getDayOfWeek();

        if (appointmentDate.isBefore(configuration.getBookingWindowStart())
                || appointmentDate.isAfter(configuration.getBookingWindowEnd())) {
            throw new BusinessRuleException("Programarea trebuie sa fie in fereastra configurata.");
        }
        if (!configuration.getConsultationDays().contains(dayOfWeek)) {
            throw new BusinessRuleException("In ziua selectata nu se efectueaza consultatii.");
        }

        LocalDateTime slotEnd = slotStart.plusMinutes(configuration.getSlotDurationMinutes());
        LocalTime startTime = slotStart.toLocalTime();
        LocalTime endTime = slotEnd.toLocalTime();

        if (startTime.isBefore(configuration.getConsultationStartTime())
                || endTime.isAfter(configuration.getConsultationEndTime())) {
            throw new BusinessRuleException("Intervalul selectat este in afara programului de consultatii.");
        }
        if (overlapsLunchBreak(startTime, endTime, configuration)) {
            throw new BusinessRuleException("Intervalul selectat se suprapune peste pauza de masa.");
        }

        long minutesFromStart = Duration.between(configuration.getConsultationStartTime(), startTime).toMinutes();
        if (minutesFromStart < 0 || minutesFromStart % configuration.getSlotDurationMinutes() != 0) {
            throw new BusinessRuleException("Intervalul selectat nu respecta grila de programare.");
        }
    }

    private boolean overlapsLunchBreak(LocalTime slotStart, LocalTime slotEnd, SchedulingConfiguration configuration) {
        if (configuration.getLunchBreakStart() == null || configuration.getLunchBreakEnd() == null) {
            return false;
        }
        return slotStart.isBefore(configuration.getLunchBreakEnd())
                && slotEnd.isAfter(configuration.getLunchBreakStart());
    }

    private boolean isFree(LocalDateTime slotStart, LocalDateTime slotEnd, List<Appointment> appointments) {
        return appointments.stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.ACTIVE)
                .noneMatch(appointment -> appointment.getAppointmentStart().isBefore(slotEnd)
                        && appointment.getAppointmentEnd().isAfter(slotStart));
    }

    private LocalDate max(LocalDate first, LocalDate second) {
        return first.isAfter(second) ? first : second;
    }

    private LocalDate min(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }
}
