package ro.company.visionslot.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.company.visionslot.dto.AppointmentReportResponse;
import ro.company.visionslot.entity.Appointment;
import ro.company.visionslot.entity.SchedulingConfiguration;
import ro.company.visionslot.entity.enums.AppointmentStatus;
import ro.company.visionslot.exception.BusinessRuleException;
import ro.company.visionslot.repository.AppointmentRepository;

@Service
public class ReportService {

    private final AppointmentRepository appointmentRepository;
    private final SchedulingConfigurationService schedulingConfigurationService;

    public ReportService(
            AppointmentRepository appointmentRepository,
            SchedulingConfigurationService schedulingConfigurationService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.schedulingConfigurationService = schedulingConfigurationService;
    }

    @Transactional(readOnly = true)
    public List<AppointmentReportResponse> getPrintableAppointments(LocalDate startDate, LocalDate endDate) {
        SchedulingConfiguration configuration = schedulingConfigurationService.getRequiredConfiguration();
        LocalDate effectiveStart = startDate == null ? configuration.getBookingWindowStart() : startDate;
        LocalDate effectiveEnd = endDate == null ? configuration.getBookingWindowEnd() : endDate;

        if (effectiveStart.isAfter(effectiveEnd)) {
            throw new BusinessRuleException("Intervalul de raportare este invalid.");
        }

        return appointmentRepository
                .findByStatusAndAppointmentStartGreaterThanEqualAndAppointmentStartLessThanOrderByAppointmentStart(
                        AppointmentStatus.ACTIVE,
                        effectiveStart.atStartOfDay(),
                        effectiveEnd.plusDays(1).atStartOfDay()
                )
                .stream()
                .map(this::toReportResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportAppointmentsToExcel(LocalDate startDate, LocalDate endDate) {
        List<AppointmentReportResponse> reportRows = getPrintableAppointments(startDate, endDate);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Programari");
            writeHeader(sheet.createRow(0));

            int rowIndex = 1;
            for (AppointmentReportResponse reportRow : reportRows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(reportRow.date().toString());
                row.createCell(1).setCellValue(reportRow.startTime().toString());
                row.createCell(2).setCellValue(reportRow.endTime().toString());
                row.createCell(3).setCellValue(reportRow.employeeCode());
                row.createCell(4).setCellValue(reportRow.fullName());
                row.createCell(5).setCellValue(reportRow.email() == null ? "" : reportRow.email());
                row.createCell(6).setCellValue(reportRow.participantType().name());
                row.createCell(7).setCellValue(reportRow.bookingSource().name());
                row.createCell(8).setCellValue(reportRow.status().name());
            }

            for (int column = 0; column < 9; column++) {
                sheet.autoSizeColumn(column);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Nu s-a putut genera fisierul Excel.", exception);
        }
    }

    private void writeHeader(Row headerRow) {
        headerRow.createCell(0).setCellValue("Data");
        headerRow.createCell(1).setCellValue("Ora inceput");
        headerRow.createCell(2).setCellValue("Ora sfarsit");
        headerRow.createCell(3).setCellValue("Marca");
        headerRow.createCell(4).setCellValue("Nume");
        headerRow.createCell(5).setCellValue("Email");
        headerRow.createCell(6).setCellValue("Tip participant");
        headerRow.createCell(7).setCellValue("Sursa");
        headerRow.createCell(8).setCellValue("Status");
    }

    private AppointmentReportResponse toReportResponse(Appointment appointment) {
        LocalDateTime appointmentStart = appointment.getAppointmentStart();
        LocalDateTime appointmentEnd = appointment.getAppointmentEnd();
        return new AppointmentReportResponse(
                appointmentStart.toLocalDate(),
                appointmentStart.toLocalTime(),
                appointmentEnd.toLocalTime(),
                appointment.getEmployeeCode(),
                appointment.getFullName(),
                appointment.getEmail(),
                appointment.getParticipantType(),
                appointment.getBookingSource(),
                appointment.getStatus()
        );
    }
}
