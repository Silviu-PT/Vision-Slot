package ro.company.visionslot.mapper;

import ro.company.visionslot.dto.AppointmentResponse;
import ro.company.visionslot.entity.Appointment;

public final class AppointmentMapper {

    private AppointmentMapper() {
    }

    public static AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getEmployeeCode(),
                appointment.getFullName(),
                appointment.getEmail(),
                appointment.getParticipantType(),
                appointment.getBookingSource(),
                appointment.getStatus(),
                appointment.getAppointmentStart(),
                appointment.getAppointmentEnd(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
