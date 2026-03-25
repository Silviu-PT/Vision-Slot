package ro.company.visionslot.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.company.visionslot.dto.AppointmentCreateRequest;
import ro.company.visionslot.dto.AppointmentRescheduleRequest;
import ro.company.visionslot.dto.AppointmentResponse;
import ro.company.visionslot.dto.ConfirmationResponse;
import ro.company.visionslot.dto.NotificationResponse;
import ro.company.visionslot.dto.TimeIntervalResponse;
import ro.company.visionslot.entity.Appointment;
import ro.company.visionslot.entity.enums.AppointmentStatus;
import ro.company.visionslot.entity.enums.NotificationEventType;
import ro.company.visionslot.exception.ConflictException;
import ro.company.visionslot.exception.ResourceNotFoundException;
import ro.company.visionslot.mapper.AppointmentMapper;
import ro.company.visionslot.repository.AppointmentRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AvailabilityService availabilityService;
    private final NotificationService notificationService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AvailabilityService availabilityService,
            NotificationService notificationService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityService = availabilityService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ConfirmationResponse createAppointment(AppointmentCreateRequest request) {
        String employeeCode = normalize(request.employeeCode());
        if (appointmentRepository.existsByEmployeeCodeAndStatus(employeeCode, AppointmentStatus.ACTIVE)) {
            throw new ConflictException("Exista deja o programare activa pentru marca specificata.");
        }

        TimeIntervalResponse slot = availabilityService.validateAndResolveSlot(request.appointmentStart(), null);
        Appointment appointment = new Appointment();
        appointment.setEmployeeCode(employeeCode);
        appointment.setFullName(normalize(request.fullName()));
        appointment.setEmail(normalizeNullable(request.email()));
        appointment.setParticipantType(request.participantType());
        appointment.setBookingSource(request.bookingSource());
        appointment.setStatus(AppointmentStatus.ACTIVE);
        appointment.setAppointmentStart(slot.start());
        appointment.setAppointmentEnd(slot.end());

        Appointment savedAppointment = appointmentRepository.save(appointment);
        List<NotificationResponse> notifications = notificationService.publish(savedAppointment, NotificationEventType.CREATED);
        return new ConfirmationResponse(
                "Programarea a fost inregistrata cu succes.",
                AppointmentMapper.toResponse(savedAppointment),
                notifications
        );
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(UUID appointmentId) {
        return AppointmentMapper.toResponse(getExistingAppointment(appointmentId));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getActiveAppointmentForEmployee(String employeeCode) {
        Appointment appointment = appointmentRepository.findByEmployeeCodeAndStatus(
                        normalize(employeeCode),
                        AppointmentStatus.ACTIVE
                )
                .orElseThrow(() -> new ResourceNotFoundException("Nu exista o programare activa pentru marca trimisa."));
        return AppointmentMapper.toResponse(appointment);
    }

    @Transactional
    public ConfirmationResponse rescheduleAppointment(UUID appointmentId, AppointmentRescheduleRequest request) {
        Appointment appointment = getExistingAppointment(appointmentId);
        ensureActive(appointment);

        TimeIntervalResponse slot = availabilityService.validateAndResolveSlot(request.newAppointmentStart(), appointmentId);
        appointment.setAppointmentStart(slot.start());
        appointment.setAppointmentEnd(slot.end());

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        List<NotificationResponse> notifications = notificationService.publish(
                updatedAppointment,
                NotificationEventType.RESCHEDULED
        );
        return new ConfirmationResponse(
                "Programarea a fost modificata cu succes.",
                AppointmentMapper.toResponse(updatedAppointment),
                notifications
        );
    }

    @Transactional
    public ConfirmationResponse cancelAppointment(UUID appointmentId) {
        Appointment appointment = getExistingAppointment(appointmentId);
        ensureActive(appointment);
        appointment.setStatus(AppointmentStatus.CANCELLED);

        Appointment cancelledAppointment = appointmentRepository.save(appointment);
        List<NotificationResponse> notifications = notificationService.publish(
                cancelledAppointment,
                NotificationEventType.CANCELLED
        );
        return new ConfirmationResponse(
                "Programarea a fost anulata cu succes.",
                AppointmentMapper.toResponse(cancelledAppointment),
                notifications
        );
    }

    private Appointment getExistingAppointment(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Programarea solicitata nu exista."));
    }

    private void ensureActive(Appointment appointment) {
        if (appointment.getStatus() != AppointmentStatus.ACTIVE) {
            throw new ConflictException("Programarea nu mai este activa.");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
