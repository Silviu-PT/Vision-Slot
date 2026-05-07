package ro.proiectcolectiv.ophthalmology.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ro.proiectcolectiv.ophthalmology.dto.AvailableSlotResponse;

@Repository
public class SlotRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SlotRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AvailableSlotResponse> findAvailableSlots(LocalDate from, LocalDate to, LocalDate today) {
        String sql = """
                SELECT s.id, s.slot_start, s.slot_end
                FROM appointment_slots s
                INNER JOIN consultation_days d ON d.id = s.consultation_day_id
                INNER JOIN booking_campaigns c ON c.id = d.campaign_id
                WHERE s.status = 'AVAILABLE'
                  AND c.status = 'PUBLISHED'
                  AND CAST(s.slot_start AS DATE) BETWEEN :fromDate AND :toDate
                  AND :today BETWEEN c.booking_start_date AND c.booking_end_date
                ORDER BY s.slot_start
                """;

        Map<String, Object> params = Map.of(
                "fromDate", from,
                "toDate", to,
                "today", today);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new AvailableSlotResponse(
                rs.getLong("id"),
                rs.getTimestamp("slot_start").toLocalDateTime(),
                rs.getTimestamp("slot_end").toLocalDateTime()));
    }

    public boolean markSlotAsBooked(long slotId) {
        String sql = """
                UPDATE appointment_slots
                SET status = 'BOOKED'
                WHERE id = :slotId
                  AND status = 'AVAILABLE'
                """;
        return jdbcTemplate.update(sql, Map.of("slotId", slotId)) == 1;
    }

    public Optional<Long> findOpenCampaignIdBySlotId(long slotId, LocalDate today) {
        String sql = """
                SELECT c.id
                FROM appointment_slots s
                INNER JOIN consultation_days d ON d.id = s.consultation_day_id
                INNER JOIN booking_campaigns c ON c.id = d.campaign_id
                WHERE s.id = :slotId
                  AND c.status = 'PUBLISHED'
                  AND :today BETWEEN c.booking_start_date AND c.booking_end_date
                """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sql,
                    Map.of("slotId", slotId, "today", today),
                    Long.class));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public boolean markSlotAsAvailable(long slotId) {
        String sql = """
                UPDATE appointment_slots
                SET status = 'AVAILABLE'
                WHERE id = :slotId
                  AND status = 'BOOKED'
                """;
        return jdbcTemplate.update(sql, Map.of("slotId", slotId)) == 1;
    }

    public void createSlot(long consultationDayId, LocalDateTime slotStart, LocalDateTime slotEnd) {
        String sql = """
                INSERT INTO appointment_slots (consultation_day_id, slot_start, slot_end, status)
                VALUES (:consultationDayId, :slotStart, :slotEnd, 'AVAILABLE')
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("consultationDayId", consultationDayId)
                .addValue("slotStart", Timestamp.valueOf(slotStart))
                .addValue("slotEnd", Timestamp.valueOf(slotEnd));

        jdbcTemplate.update(sql, params);
    }
}
