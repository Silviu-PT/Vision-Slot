package ro.proiectcolectiv.ophthalmology.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import ro.proiectcolectiv.ophthalmology.model.Campaign;

@Repository
public class CampaignRepository {

    private static final RowMapper<Campaign> CAMPAIGN_MAPPER = (rs, rowNum) -> new Campaign(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getDate("booking_start_date").toLocalDate(),
            rs.getDate("booking_end_date").toLocalDate(),
            rs.getString("status"),
            rs.getInt("consultation_day_count"),
            rs.getInt("slot_count"));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CampaignRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long createCampaign(String name, LocalDate bookingStartDate, LocalDate bookingEndDate) {
        String sql = """
                INSERT INTO booking_campaigns (name, booking_start_date, booking_end_date, status)
                VALUES (:name, :bookingStartDate, :bookingEndDate, 'DRAFT')
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("bookingStartDate", bookingStartDate)
                .addValue("bookingEndDate", bookingEndDate);

        jdbcTemplate.update(sql, params, keyHolder, new String[] {"id"});

        Number campaignId = keyHolder.getKey();
        if (campaignId == null) {
            throw new IllegalStateException("Nu s-a putut obtine id-ul campaniei create.");
        }
        return campaignId.longValue();
    }

    public long createConsultationDay(
            long campaignId,
            LocalDate consultationDate,
            LocalTime startTime,
            LocalTime endTime,
            LocalTime lunchStart,
            LocalTime lunchEnd,
            int slotDurationMinutes) {
        String sql = """
                INSERT INTO consultation_days
                    (campaign_id, consultation_date, start_time, end_time, lunch_start, lunch_end, slot_duration_minutes)
                VALUES
                    (:campaignId, :consultationDate, :startTime, :endTime, :lunchStart, :lunchEnd, :slotDurationMinutes)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("campaignId", campaignId)
                .addValue("consultationDate", consultationDate)
                .addValue("startTime", startTime)
                .addValue("endTime", endTime)
                .addValue("lunchStart", lunchStart)
                .addValue("lunchEnd", lunchEnd)
                .addValue("slotDurationMinutes", slotDurationMinutes);

        jdbcTemplate.update(sql, params, keyHolder, new String[] {"id"});

        Number consultationDayId = keyHolder.getKey();
        if (consultationDayId == null) {
            throw new IllegalStateException("Nu s-a putut obtine id-ul zilei de consultatii create.");
        }
        return consultationDayId.longValue();
    }

    public List<Campaign> findAll() {
        return jdbcTemplate.query(baseCampaignSelect() + " ORDER BY created_at DESC", CAMPAIGN_MAPPER);
    }

    public Optional<Campaign> findById(long campaignId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    baseCampaignSelect() + " WHERE c.id = :campaignId",
                    Map.of("campaignId", campaignId),
                    CAMPAIGN_MAPPER));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<Campaign> findCurrentOpenPublished(LocalDate today) {
        String sql = baseCampaignSelect() + """
                WHERE c.status = 'PUBLISHED'
                  AND :today BETWEEN c.booking_start_date AND c.booking_end_date
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, Map.of("today", today), CAMPAIGN_MAPPER));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public boolean hasOtherPublishedCampaign(long campaignId) {
        String sql = """
                SELECT COUNT(*)
                FROM booking_campaigns
                WHERE status = 'PUBLISHED'
                  AND id <> :campaignId
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Map.of("campaignId", campaignId), Integer.class);
        return count != null && count > 0;
    }

    public boolean isCampaignOpen(long campaignId, LocalDate today) {
        String sql = """
                SELECT COUNT(*)
                FROM booking_campaigns
                WHERE id = :campaignId
                  AND status = 'PUBLISHED'
                  AND :today BETWEEN booking_start_date AND booking_end_date
                """;
        Integer count = jdbcTemplate.queryForObject(
                sql,
                Map.of("campaignId", campaignId, "today", today),
                Integer.class);
        return count != null && count == 1;
    }

    public void publish(long campaignId) {
        String sql = """
                UPDATE booking_campaigns
                SET status = 'PUBLISHED'
                WHERE id = :campaignId
                  AND status = 'DRAFT'
                """;
        jdbcTemplate.update(sql, Map.of("campaignId", campaignId));
    }

    public void close(long campaignId) {
        String sql = """
                UPDATE booking_campaigns
                SET status = 'CLOSED'
                WHERE id = :campaignId
                  AND status IN ('DRAFT', 'PUBLISHED')
                """;
        jdbcTemplate.update(sql, Map.of("campaignId", campaignId));
    }

    private String baseCampaignSelect() {
        return """
                SELECT c.id,
                       c.name,
                       c.booking_start_date,
                       c.booking_end_date,
                       c.status,
                       (
                           SELECT COUNT(*)
                           FROM consultation_days d
                           WHERE d.campaign_id = c.id
                       ) AS consultation_day_count,
                       (
                           SELECT COUNT(*)
                           FROM appointment_slots s
                           INNER JOIN consultation_days d ON d.id = s.consultation_day_id
                           WHERE d.campaign_id = c.id
                       ) AS slot_count,
                       c.created_at
                FROM booking_campaigns c
                """;
    }
}
