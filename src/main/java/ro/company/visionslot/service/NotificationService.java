package ro.company.visionslot.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.company.visionslot.dto.NotificationResponse;
import ro.company.visionslot.entity.Appointment;
import ro.company.visionslot.entity.NotificationLog;
import ro.company.visionslot.entity.enums.NotificationChannel;
import ro.company.visionslot.entity.enums.NotificationEventType;
import ro.company.visionslot.repository.NotificationLogRepository;

@Service
public class NotificationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final NotificationLogRepository notificationLogRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String emailFrom;

    public NotificationService(
            NotificationLogRepository notificationLogRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${visionslot.notification.email.from:no-reply@visionslot.local}") String emailFrom
    ) {
        this.notificationLogRepository = notificationLogRepository;
        this.mailSenderProvider = mailSenderProvider;
        this.emailFrom = emailFrom;
    }

    @Transactional
    public List<NotificationResponse> publish(Appointment appointment, NotificationEventType eventType) {
        List<NotificationResponse> responses = new ArrayList<>();
        String message = buildMessage(appointment, eventType);

        NotificationLog visualLog = new NotificationLog();
        visualLog.setAppointment(appointment);
        visualLog.setChannel(NotificationChannel.VISUAL);
        visualLog.setEventType(eventType);
        visualLog.setRecipient(appointment.getEmployeeCode());
        visualLog.setMessage(message);
        visualLog.setDelivered(true);
        notificationLogRepository.save(visualLog);
        responses.add(new NotificationResponse(NotificationChannel.VISUAL, true, appointment.getEmployeeCode(), message));

        if (appointment.getEmail() == null || appointment.getEmail().isBlank()) {
            return responses;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            NotificationLog failedLog = new NotificationLog();
            failedLog.setAppointment(appointment);
            failedLog.setChannel(NotificationChannel.EMAIL);
            failedLog.setEventType(eventType);
            failedLog.setRecipient(appointment.getEmail());
            failedLog.setMessage(message);
            failedLog.setDelivered(false);
            failedLog.setFailureReason("SMTP neconfigurat.");
            notificationLogRepository.save(failedLog);
            responses.add(new NotificationResponse(
                    NotificationChannel.EMAIL,
                    false,
                    appointment.getEmail(),
                    "Confirmarea prin e-mail nu a fost trimisa deoarece SMTP nu este configurat."
            ));
            return responses;
        }

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(emailFrom);
            mailMessage.setTo(appointment.getEmail());
            mailMessage.setSubject(buildSubject(eventType));
            mailMessage.setText(message);
            mailSender.send(mailMessage);

            NotificationLog emailLog = new NotificationLog();
            emailLog.setAppointment(appointment);
            emailLog.setChannel(NotificationChannel.EMAIL);
            emailLog.setEventType(eventType);
            emailLog.setRecipient(appointment.getEmail());
            emailLog.setMessage(message);
            emailLog.setDelivered(true);
            notificationLogRepository.save(emailLog);
            responses.add(new NotificationResponse(NotificationChannel.EMAIL, true, appointment.getEmail(), message));
        } catch (Exception exception) {
            NotificationLog failedLog = new NotificationLog();
            failedLog.setAppointment(appointment);
            failedLog.setChannel(NotificationChannel.EMAIL);
            failedLog.setEventType(eventType);
            failedLog.setRecipient(appointment.getEmail());
            failedLog.setMessage(message);
            failedLog.setDelivered(false);
            failedLog.setFailureReason(exception.getMessage());
            notificationLogRepository.save(failedLog);
            responses.add(new NotificationResponse(
                    NotificationChannel.EMAIL,
                    false,
                    appointment.getEmail(),
                    "Trimiterea e-mailului a esuat: " + exception.getMessage()
            ));
        }

        return responses;
    }

    private String buildMessage(Appointment appointment, NotificationEventType eventType) {
        String timestamp = appointment.getAppointmentStart().format(DATE_TIME_FORMATTER);
        return switch (eventType) {
            case CREATED -> "Programarea pentru " + appointment.getFullName() + " a fost efectuata pentru "
                    + timestamp + ".";
            case RESCHEDULED -> "Programarea pentru " + appointment.getFullName() + " a fost modificata pentru "
                    + timestamp + ".";
            case CANCELLED -> "Programarea pentru " + appointment.getFullName() + " din " + timestamp
                    + " a fost anulata.";
        };
    }

    private String buildSubject(NotificationEventType eventType) {
        return switch (eventType) {
            case CREATED -> "Confirmare programare VisionSlot";
            case RESCHEDULED -> "Programare modificata VisionSlot";
            case CANCELLED -> "Programare anulata VisionSlot";
        };
    }
}
