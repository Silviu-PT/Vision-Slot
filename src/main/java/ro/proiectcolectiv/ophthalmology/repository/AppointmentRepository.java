package ro.proiectcolectiv.ophthalmology.repository;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import ro.proiectcolectiv.ophthalmology.dto.AppointmentResponse;
import ro.proiectcolectiv.ophthalmology.dto.CreateAppointmentRequest;
import ro.proiectcolectiv.ophthalmology.model.AppointmentExportRow;

@Repository
public class AppointmentRepository {

    private static final RowMapper<AppointmentResponse> APPOINTMENT_MAPPER = (rs, rowNum) -> new AppointmentResponse(
            rs.getLong("appointment_id"),
            rs.getLong("campaign_id"),
            rs.getLong("slot_id"),
            rs.getString("employee_number"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("status"),
            rs.getTimestamp("slot_start").toLocalDateTime(),
            rs.getTimestamp("slot_end").toLocalDateTime(),
            UUID.fromString(rs.getString("cancel_token")));

    private static final RowMapper<AppointmentExportRow> EXPORT_MAPPER = (rs, rowNum) -> new AppointmentExportRow(
            rs.getLong("appointment_id"),
            rs.getTimestamp("slot_start").toLocalDateTime(),
            rs.getTimestamp("slot_end").toLocalDateTime(),
            rs.getString("employee_number"),
            rs.getString("full_name"),
            rs.getString("email"));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AppointmentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AppointmentResponse create(CreateAppointmentRequest request, long campaignId) {
        String sql = """
                INSERT INTO appointments (campaign_id, slot_id, employee_number, full_name, email, status)
                VALUES (:campaignId, :slotId, :employeeNumber, :fullName, :email, 'ACTIVE')
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("campaignId", campaignId)
                .addValue("slotId", request.slotId())
                .addValue("employeeNumber", request.employeeNumber())
                .addValue("fullName", request.fullName())
                .addValue("email", request.email());

        jdbcTemplate.update(sql, params, keyHolder, new String[] {"id"});

        Number appointmentId = keyHolder.getKey();
        if (appointmentId == null) {
            throw new IllegalStateException("Nu s-a putut obtine id-ul programarii create.");
        }

        return findActiveById(appointmentId.longValue()).orElseThrow();
    }

    public Optional<AppointmentResponse> findActiveById(long appointmentId) {
        String sql = baseAppointmentSelect() + """
                WHERE a.id = :appointmentId
                  AND a.status = 'ACTIVE'
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    sql,
                    Map.of("appointmentId", appointmentId),
                    APPOINTMENT_MAPPER));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<AppointmentResponse> findActiveByToken(UUID cancelToken) {
        String sql = baseAppointmentSelect() + """
                WHERE a.cancel_token = :cancelToken
                  AND a.status = 'ACTIVE'
                """;

        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("cancelToken", cancelToken.toString(), Types.VARCHAR);
            return Optional.of(jdbcTemplate.queryForObject(
                    sql,
                    params,
                    APPOINTMENT_MAPPER));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public void updateSlot(long appointmentId, long newSlotId) {
        String sql = """
                UPDATE appointments
                SET slot_id = :newSlotId,
                    updated_at = SYSDATETIME()
                WHERE id = :appointmentId
                  AND status = 'ACTIVE'
                """;

        jdbcTemplate.update(sql, Map.of(
                "appointmentId", appointmentId,
                "newSlotId", newSlotId));
    }

    public void cancel(long appointmentId) {
        String sql = """
                UPDATE appointments
                SET status = 'CANCELED',
                    updated_at = SYSDATETIME()
                WHERE id = :appointmentId
                  AND status = 'ACTIVE'
                """;

        jdbcTemplate.update(sql, Map.of("appointmentId", appointmentId));
    }

    public List<AppointmentExportRow> findActiveAppointmentsForExport(Long campaignId) {
        String sql = """
                SELECT a.id AS appointment_id,
                       s.slot_start,
                       s.slot_end,
                       a.employee_number,
                       a.full_name,
                       a.email
                FROM appointments a
                INNER JOIN appointment_slots s ON s.id = a.slot_id
                WHERE a.status = 'ACTIVE'
                  AND (:campaignId IS NULL OR a.campaign_id = :campaignId)
                ORDER BY s.slot_start, a.full_name
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("campaignId", campaignId, Types.BIGINT);

        return jdbcTemplate.query(sql, params, EXPORT_MAPPER);
    }

    public void logNotification(long appointmentId, String type, String email, String status, String errorMessage) {
        String sql = """
                INSERT INTO notification_log
                    (appointment_id, notification_type, recipient_email, status, error_message)
                VALUES
                    (:appointmentId, :type, :email, :status, :errorMessage)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("appointmentId", appointmentId)
                .addValue("type", type)
                .addValue("email", email)
                .addValue("status", status)
                .addValue("errorMessage", errorMessage, Types.NVARCHAR);

        jdbcTemplate.update(sql, params);
    }

    private String baseAppointmentSelect() {
        return """
                SELECT a.id AS appointment_id,
                       a.campaign_id,
                       a.slot_id,
                       a.employee_number,
                       a.full_name,
                       a.email,
                       a.status,
                       a.cancel_token,
                       s.slot_start,
                       s.slot_end
                FROM appointments a
                INNER JOIN appointment_slots s ON s.id = a.slot_id
                """;
    }
}
