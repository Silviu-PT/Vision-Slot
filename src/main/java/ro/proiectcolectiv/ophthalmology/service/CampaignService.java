package ro.proiectcolectiv.ophthalmology.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.proiectcolectiv.ophthalmology.dto.CampaignResponse;
import ro.proiectcolectiv.ophthalmology.dto.ConsultationDayRequest;
import ro.proiectcolectiv.ophthalmology.dto.CreateCampaignRequest;
import ro.proiectcolectiv.ophthalmology.exception.ApiException;
import ro.proiectcolectiv.ophthalmology.model.Campaign;
import ro.proiectcolectiv.ophthalmology.repository.CampaignRepository;
import ro.proiectcolectiv.ophthalmology.repository.SlotRepository;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final SlotRepository slotRepository;

    public CampaignService(CampaignRepository campaignRepository, SlotRepository slotRepository) {
        this.campaignRepository = campaignRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public CampaignResponse createCampaign(CreateCampaignRequest request) {
        validateCampaignRequest(request);

        long campaignId = campaignRepository.createCampaign(
                request.name(),
                request.bookingStartDate(),
                request.bookingEndDate());

        int generatedSlots = 0;
        for (ConsultationDayRequest day : request.consultationDays()) {
            long consultationDayId = campaignRepository.createConsultationDay(
                    campaignId,
                    day.consultationDate(),
                    day.startTime(),
                    day.endTime(),
                    day.lunchStart(),
                    day.lunchEnd(),
                    day.slotDurationMinutes());
            generatedSlots += generateSlots(consultationDayId, day);
        }

        if (generatedSlots == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Configurarea nu genereaza niciun interval disponibil.");
        }

        return campaignRepository.findById(campaignId)
                .map(this::toResponse)
                .orElseThrow();
    }

    public List<CampaignResponse> findAll() {
        return campaignRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CampaignResponse findCurrentOpenCampaign() {
        LocalDate today = LocalDate.now();
        return campaignRepository.findCurrentOpenPublished(today)
                .map(this::toResponse)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Nu exista o campanie publicata in perioada de programare."));
    }

    @Transactional
    public CampaignResponse publish(long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Campania nu a fost gasita."));

        if (!"DRAFT".equals(campaign.status())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Doar campaniile in status DRAFT pot fi publicate.");
        }
        if (campaign.slotCount() == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Campania nu poate fi publicata fara sloturi generate.");
        }
        if (campaignRepository.hasOtherPublishedCampaign(campaignId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Exista deja o alta campanie publicata.");
        }

        campaignRepository.publish(campaignId);
        return campaignRepository.findById(campaignId)
                .map(this::toResponse)
                .orElseThrow();
    }

    @Transactional
    public CampaignResponse close(long campaignId) {
        campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Campania nu a fost gasita."));

        campaignRepository.close(campaignId);
        return campaignRepository.findById(campaignId)
                .map(this::toResponse)
                .orElseThrow();
    }

    public void ensureCampaignIsOpen(long campaignId) {
        LocalDate today = LocalDate.now();
        if (!campaignRepository.isCampaignOpen(campaignId, today)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Programarile pot fi create, modificate sau anulate doar in perioada configurata.");
        }
    }

    private int generateSlots(long consultationDayId, ConsultationDayRequest day) {
        int generatedSlots = 0;
        LocalDateTime cursor = day.consultationDate().atTime(day.startTime());
        LocalDateTime dayEnd = day.consultationDate().atTime(day.endTime());

        while (!cursor.plusMinutes(day.slotDurationMinutes()).isAfter(dayEnd)) {
            LocalDateTime slotEnd = cursor.plusMinutes(day.slotDurationMinutes());
            if (!overlapsLunch(cursor.toLocalTime(), slotEnd.toLocalTime(), day.lunchStart(), day.lunchEnd())) {
                slotRepository.createSlot(consultationDayId, cursor, slotEnd);
                generatedSlots++;
            }
            cursor = slotEnd;
        }

        return generatedSlots;
    }

    private boolean overlapsLunch(LocalTime slotStart, LocalTime slotEnd, LocalTime lunchStart, LocalTime lunchEnd) {
        if (lunchStart == null || lunchEnd == null) {
            return false;
        }
        return slotStart.isBefore(lunchEnd) && slotEnd.isAfter(lunchStart);
    }

    private void validateCampaignRequest(CreateCampaignRequest request) {
        if (request.bookingStartDate().isAfter(request.bookingEndDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Data de inceput pentru programari trebuie sa fie inaintea datei de final.");
        }

        Set<LocalDate> configuredDates = new HashSet<>();
        for (ConsultationDayRequest day : request.consultationDays()) {
            validateConsultationDay(day);
            if (!configuredDates.add(day.consultationDate())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Aceeasi zi de consultatii a fost configurata de mai multe ori.");
            }
        }
    }

    private void validateConsultationDay(ConsultationDayRequest day) {
        if (!day.startTime().isBefore(day.endTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ora de inceput trebuie sa fie inaintea orei de final.");
        }

        boolean hasOnlyOneLunchLimit = (day.lunchStart() == null) != (day.lunchEnd() == null);
        if (hasOnlyOneLunchLimit) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Pauza de masa trebuie sa aiba atat ora de inceput, cat si ora de final.");
        }

        if (day.lunchStart() != null) {
            if (!day.lunchStart().isBefore(day.lunchEnd())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Ora de inceput a pauzei trebuie sa fie inaintea orei de final.");
            }
            if (day.lunchStart().isBefore(day.startTime()) || day.lunchEnd().isAfter(day.endTime())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Pauza de masa trebuie sa fie in intervalul consultatiilor.");
            }
        }
    }

    private CampaignResponse toResponse(Campaign campaign) {
        return new CampaignResponse(
                campaign.id(),
                campaign.name(),
                campaign.bookingStartDate(),
                campaign.bookingEndDate(),
                campaign.status(),
                campaign.consultationDayCount(),
                campaign.slotCount());
    }
}
