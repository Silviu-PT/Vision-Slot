package ro.company.visionslot.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateRescheduleAndCancelAppointmentFlow() throws Exception {
        configureSchedule();

        mockMvc.perform(get("/api/slots")
                        .param("startDate", "2026-02-02")
                        .param("endDate", "2026-02-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].slots", hasSize(10)));

        MvcResult bookingResult = mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeCode": "12345",
                                  "fullName": "Ana Popescu",
                                  "email": "ana.popescu@example.com",
                                  "participantType": "EMPLOYEE",
                                  "bookingSource": "HR_PORTAL",
                                  "appointmentStart": "2026-02-02T09:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointment.employeeCode", is("12345")))
                .andExpect(jsonPath("$.appointment.status", is("ACTIVE")))
                .andReturn();

        String appointmentId = JsonTestUtils.extractJsonPath(bookingResult.getResponse().getContentAsString(), "$.appointment.id");

        mockMvc.perform(put("/api/appointments/{appointmentId}", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newAppointmentStart": "2026-02-02T13:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointment.appointmentStart", is("2026-02-02T13:00:00")));

        mockMvc.perform(delete("/api/appointments/{appointmentId}", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointment.status", is("CANCELLED")));
    }

    @Test
    void shouldExposePrintableReportForActiveAppointments() throws Exception {
        configureSchedule();

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeCode": "77889",
                                  "fullName": "Mihai Ionescu",
                                  "participantType": "AGENCY",
                                  "bookingSource": "LINK",
                                  "appointmentStart": "2026-02-02T10:00:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reports/appointments")
                        .param("startDate", "2026-02-01")
                        .param("endDate", "2026-02-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].employeeCode", is("77889")))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    private void configureSchedule() throws Exception {
        mockMvc.perform(put("/api/admin/configuration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "consultationDays": ["MONDAY", "WEDNESDAY"],
                                  "consultationStartTime": "09:00:00",
                                  "consultationEndTime": "15:00:00",
                                  "lunchBreakStart": "12:00:00",
                                  "lunchBreakEnd": "13:00:00",
                                  "slotDurationMinutes": 30,
                                  "bookingWindowStart": "2026-02-01",
                                  "bookingWindowEnd": "2026-02-15"
                                }
                                """))
                .andExpect(status().isOk());
    }
}
