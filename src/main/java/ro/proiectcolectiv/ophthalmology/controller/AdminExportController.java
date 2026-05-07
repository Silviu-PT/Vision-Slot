package ro.proiectcolectiv.ophthalmology.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ro.proiectcolectiv.ophthalmology.service.ExcelExportService;

@RestController
@RequestMapping("/api/admin/appointments")
public class AdminExportController {

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExcelExportService excelExportService;

    public AdminExportController(ExcelExportService excelExportService) {
        this.excelExportService = excelExportService;
    }

    @GetMapping(value = "/export.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportAppointments(@RequestParam(required = false) Long campaignId) {
        byte[] content = excelExportService.exportActiveAppointments(campaignId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(XLSX_MEDIA_TYPE);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("programari-oftalmologie.xlsx")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
