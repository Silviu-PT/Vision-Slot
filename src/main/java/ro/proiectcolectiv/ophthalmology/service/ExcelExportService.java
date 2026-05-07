package ro.proiectcolectiv.ophthalmology.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import ro.proiectcolectiv.ophthalmology.model.AppointmentExportRow;
import ro.proiectcolectiv.ophthalmology.repository.AppointmentRepository;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] HEADERS = {"Data", "Ora inceput", "Ora final", "Marca", "Nume", "Email"};

    private final AppointmentRepository appointmentRepository;

    public ExcelExportService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public byte[] exportActiveAppointments(Long campaignId) {
        List<AppointmentExportRow> appointments = appointmentRepository.findActiveAppointmentsForExport(campaignId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Programari");
            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            for (int column = 0; column < HEADERS.length; column++) {
                headerRow.createCell(column).setCellValue(HEADERS[column]);
                headerRow.getCell(column).setCellStyle(headerStyle);
            }

            for (int index = 0; index < appointments.size(); index++) {
                AppointmentExportRow appointment = appointments.get(index);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(appointment.slotStart().toLocalDate().format(DATE_FORMAT));
                row.createCell(1).setCellValue(appointment.slotStart().toLocalTime().format(TIME_FORMAT));
                row.createCell(2).setCellValue(appointment.slotEnd().toLocalTime().format(TIME_FORMAT));
                row.createCell(3).setCellValue(appointment.employeeNumber());
                row.createCell(4).setCellValue(appointment.fullName());
                row.createCell(5).setCellValue(appointment.email());
            }

            for (int column = 0; column < HEADERS.length; column++) {
                sheet.autoSizeColumn(column);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Nu s-a putut genera fisierul Excel.", exception);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }
}
