package ro.proiectcolectiv.ophthalmology.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ro.proiectcolectiv.ophthalmology.dto.AvailableSlotResponse;
import ro.proiectcolectiv.ophthalmology.exception.ApiException;
import ro.proiectcolectiv.ophthalmology.repository.SlotRepository;

@Service
public class SlotService {

    private final SlotRepository slotRepository;

    public SlotService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public List<AvailableSlotResponse> findAvailableSlots(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Data de inceput trebuie sa fie inaintea datei de final.");
        }

        return slotRepository.findAvailableSlots(from, to, LocalDate.now());
    }
}
