package ro.proiectcolectiv.ophthalmology.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ro.proiectcolectiv.ophthalmology.dto.AppointmentResponse;
import ro.proiectcolectiv.ophthalmology.dto.UpdateAppointmentRequest;
import ro.proiectcolectiv.ophthalmology.service.AppointmentService;

@RestController
@RequestMapping("/api/admin/appointments")
public class AdminAppointmentController {

    private final AppointmentService appointmentService;

    public AdminAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PutMapping("/{appointmentId}")
    public AppointmentResponse update(
            @PathVariable long appointmentId,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        return appointmentService.update(appointmentId, request);
    }

    @DeleteMapping("/{appointmentId}")
    public AppointmentResponse cancel(@PathVariable long appointmentId) {
        return appointmentService.cancel(appointmentId);
    }
}
