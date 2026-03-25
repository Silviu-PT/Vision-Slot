package ro.company.visionslot.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.company.visionslot.dto.AppointmentCreateRequest;
import ro.company.visionslot.dto.AppointmentRescheduleRequest;
import ro.company.visionslot.dto.AppointmentResponse;
import ro.company.visionslot.dto.ConfirmationResponse;
import ro.company.visionslot.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConfirmationResponse createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        return appointmentService.createAppointment(request);
    }

    @GetMapping("/{appointmentId}")
    public AppointmentResponse getAppointment(@PathVariable UUID appointmentId) {
        return appointmentService.getAppointment(appointmentId);
    }

    @GetMapping("/by-employee/{employeeCode}")
    public AppointmentResponse getActiveAppointmentForEmployee(@PathVariable String employeeCode) {
        return appointmentService.getActiveAppointmentForEmployee(employeeCode);
    }

    @PutMapping("/{appointmentId}")
    public ConfirmationResponse rescheduleAppointment(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody AppointmentRescheduleRequest request
    ) {
        return appointmentService.rescheduleAppointment(appointmentId, request);
    }

    @DeleteMapping("/{appointmentId}")
    public ConfirmationResponse cancelAppointment(@PathVariable UUID appointmentId) {
        return appointmentService.cancelAppointment(appointmentId);
    }
}
