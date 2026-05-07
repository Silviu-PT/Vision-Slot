package ro.proiectcolectiv.ophthalmology.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import ro.proiectcolectiv.ophthalmology.dto.AppointmentResponse;
import ro.proiectcolectiv.ophthalmology.repository.AppointmentRepository;

@Service
public class NotificationService {

    private final RestClient restClient;
    private final AppointmentRepository appointmentRepository;
    private final boolean notificationApiEnabled;
    private final String notificationApiUrl;
    private final String notificationApiToken;
    private final String frontendBaseUrl;

    public NotificationService(
            RestClient.Builder restClientBuilder,
            AppointmentRepository appointmentRepository,
            @Value("${notification.api.enabled}") boolean notificationApiEnabled,
            @Value("${notification.api.url}") String notificationApiUrl,
            @Value("${notification.api.token}") String notificationApiToken,
            @Value("${app.frontend.base-url}") String frontendBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.appointmentRepository = appointmentRepository;
        this.notificationApiEnabled = notificationApiEnabled;
        this.notificationApiUrl = notificationApiUrl;
        this.notificationApiToken = notificationApiToken;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public void sendCreated(AppointmentResponse appointment) {
        send(appointment, "CREATED", "Confirmare programare oftalmologie",
                """
                Programarea a fost creata pentru %s.

                Pentru modificare sau anulare, folositi linkul:
                %s/appointments/%s
                """.formatted(appointment.slotStart(), frontendBaseUrl, appointment.cancelToken()));
    }

    public void sendUpdated(AppointmentResponse appointment) {
        send(appointment, "UPDATED", "Modificare programare oftalmologie",
                """
                Programarea a fost modificata pentru %s.

                Pentru modificare sau anulare, folositi linkul:
                %s/appointments/%s
                """.formatted(appointment.slotStart(), frontendBaseUrl, appointment.cancelToken()));
    }

    public void sendCanceled(AppointmentResponse appointment) {
        send(appointment, "CANCELED", "Anulare programare oftalmologie",
                "Programarea pentru " + appointment.slotStart() + " a fost anulata.");
    }

    private void send(AppointmentResponse appointment, String type, String title, String body) {
        if (!notificationApiEnabled) {
            appointmentRepository.logNotification(
                    appointment.appointmentId(),
                    type,
                    appointment.email(),
                    "SKIPPED",
                    "Notification API dezactivat prin notification.api.enabled=false");
            return;
        }

        if (notificationApiUrl.isBlank() || notificationApiToken.isBlank()) {
            appointmentRepository.logNotification(
                    appointment.appointmentId(),
                    type,
                    appointment.email(),
                    "FAILED",
                    "Notification API nu este configurat complet.");
            return;
        }

        try {
            NotificationApiResponse response = restClient.post()
                    .uri(notificationApiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + notificationApiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new NotificationApiRequest(title, appointment.email(), body))
                    .retrieve()
                    .body(NotificationApiResponse.class);

            if (response != null && Boolean.TRUE.equals(response.success())) {
                appointmentRepository.logNotification(appointment.appointmentId(), type, appointment.email(), "SENT", null);
            } else {
                String message = response == null ? "Raspuns gol de la Notification API." : response.message();
                appointmentRepository.logNotification(appointment.appointmentId(), type, appointment.email(), "FAILED", trim(message));
            }
        } catch (RestClientException exception) {
            appointmentRepository.logNotification(
                    appointment.appointmentId(),
                    type,
                    appointment.email(),
                    "FAILED",
                    trim(exception.getMessage()));
        }
    }

    private String trim(String message) {
        if (message == null || message.length() <= 1000) {
            return message;
        }
        return message.substring(0, 1000);
    }

    private record NotificationApiRequest(String title, String recipient, String body) {
    }

    private record NotificationApiResponse(Boolean success, String message) {
    }
}
