package ro.proiectcolectiv.ophthalmology.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.proiectcolectiv.ophthalmology.dto.AppointmentResponse;
import ro.proiectcolectiv.ophthalmology.dto.CreateAppointmentRequest;
import ro.proiectcolectiv.ophthalmology.dto.UpdateAppointmentRequest;
import ro.proiectcolectiv.ophthalmology.exception.ApiException;
import ro.proiectcolectiv.ophthalmology.repository.AppointmentRepository;
import ro.proiectcolectiv.ophthalmology.repository.SlotRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final NotificationService notificationService;
    private final CampaignService campaignService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            SlotRepository slotRepository,
            NotificationService notificationService,
            CampaignService campaignService) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.notificationService = notificationService;
        this.campaignService = campaignService;
    }

    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {
        long campaignId = slotRepository.findOpenCampaignIdBySlotId(request.slotId(), LocalDate.now())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.FORBIDDEN,
                        "Programarile nu sunt deschise pentru intervalul selectat."));

        boolean slotBooked = slotRepository.markSlotAsBooked(request.slotId());
        if (!slotBooked) {
            throw new ApiException(HttpStatus.CONFLICT, "Intervalul selectat nu mai este disponibil.");
        }

        try {
            AppointmentResponse appointment = appointmentRepository.create(request, campaignId);
            notificationService.sendCreated(appointment);
            return appointment;
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Exista deja o programare activa pentru aceasta marca sau pentru acest interval.");
        }
    }

    @Transactional
    public AppointmentResponse update(long appointmentId, UpdateAppointmentRequest request) {
        AppointmentResponse existingAppointment = appointmentRepository.findActiveById(appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Programarea nu a fost gasita."));
        return updateExistingAppointment(existingAppointment, request.newSlotId());
    }

    @Transactional
    public AppointmentResponse updateByToken(UUID cancelToken, UpdateAppointmentRequest request) {
        AppointmentResponse existingAppointment = appointmentRepository.findActiveByToken(cancelToken)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Programarea nu a fost gasita."));
        return updateExistingAppointment(existingAppointment, request.newSlotId());
    }

    @Transactional
    public AppointmentResponse cancel(long appointmentId) {
        AppointmentResponse appointment = appointmentRepository.findActiveById(appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Programarea nu a fost gasita."));
        return cancelExistingAppointment(appointment);
    }

    @Transactional
    public AppointmentResponse cancelByToken(UUID cancelToken) {
        AppointmentResponse appointment = appointmentRepository.findActiveByToken(cancelToken)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Programarea nu a fost gasita."));
        return cancelExistingAppointment(appointment);
    }

    private AppointmentResponse updateExistingAppointment(AppointmentResponse existingAppointment, long newSlotId) {
        campaignService.ensureCampaignIsOpen(existingAppointment.campaignId());
        if (existingAppointment.slotId() == newSlotId) {
            return existingAppointment;
        }

        long newSlotCampaignId = slotRepository.findOpenCampaignIdBySlotId(newSlotId, LocalDate.now())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.FORBIDDEN,
                        "Programarile nu sunt deschise pentru noul interval selectat."));
        if (newSlotCampaignId != existingAppointment.campaignId()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Programarea poate fi mutata doar in aceeasi campanie.");
        }

        boolean newSlotBooked = slotRepository.markSlotAsBooked(newSlotId);
        if (!newSlotBooked) {
            throw new ApiException(HttpStatus.CONFLICT, "Noul interval selectat nu mai este disponibil.");
        }

        appointmentRepository.updateSlot(existingAppointment.appointmentId(), newSlotId);
        slotRepository.markSlotAsAvailable(existingAppointment.slotId());

        AppointmentResponse updatedAppointment = appointmentRepository.findActiveById(existingAppointment.appointmentId())
                .orElseThrow();
        notificationService.sendUpdated(updatedAppointment);
        return updatedAppointment;
    }

    private AppointmentResponse cancelExistingAppointment(AppointmentResponse appointment) {
        campaignService.ensureCampaignIsOpen(appointment.campaignId());
        appointmentRepository.cancel(appointment.appointmentId());
        slotRepository.markSlotAsAvailable(appointment.slotId());
        notificationService.sendCanceled(appointment);

        return new AppointmentResponse(
                appointment.appointmentId(),
                appointment.campaignId(),
                appointment.slotId(),
                appointment.employeeNumber(),
                appointment.fullName(),
                appointment.email(),
                "CANCELED",
                appointment.slotStart(),
                appointment.slotEnd(),
                appointment.cancelToken());
    }
}
