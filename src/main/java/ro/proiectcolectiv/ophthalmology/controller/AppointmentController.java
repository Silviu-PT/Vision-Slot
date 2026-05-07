package ro.proiectcolectiv.ophthalmology.controller;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ro.proiectcolectiv.ophthalmology.dto.AppointmentResponse;
import ro.proiectcolectiv.ophthalmology.dto.CreateAppointmentRequest;
import ro.proiectcolectiv.ophthalmology.dto.UpdateAppointmentRequest;
import ro.proiectcolectiv.ophthalmology.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public AppointmentResponse create(@Valid @RequestBody CreateAppointmentRequest request) {
        return appointmentService.create(request);
    }

    @PutMapping("/token/{cancelToken}")
    public AppointmentResponse updateByToken(
            @PathVariable UUID cancelToken,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        return appointmentService.updateByToken(cancelToken, request);
    }

    @DeleteMapping("/token/{cancelToken}")
    public AppointmentResponse cancelByToken(@PathVariable UUID cancelToken) {
        return appointmentService.cancelByToken(cancelToken);
    }
}
