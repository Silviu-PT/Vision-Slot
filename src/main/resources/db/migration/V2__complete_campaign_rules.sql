ALTER TABLE appointments ADD campaign_id BIGINT NULL;

UPDATE a
SET campaign_id = d.campaign_id
FROM appointments a
INNER JOIN appointment_slots s ON s.id = a.slot_id
INNER JOIN consultation_days d ON d.id = s.consultation_day_id;

ALTER TABLE appointments ALTER COLUMN campaign_id BIGINT NOT NULL;

ALTER TABLE appointments ADD CONSTRAINT fk_appointments_campaign
    FOREIGN KEY (campaign_id) REFERENCES booking_campaigns(id);

DROP INDEX uq_appointments_employee_active ON appointments;

CREATE UNIQUE INDEX uq_appointments_campaign_employee_active
    ON appointments(campaign_id, employee_number)
    WHERE status = 'ACTIVE';

DROP INDEX uq_appointment_slots_start ON appointment_slots;

CREATE UNIQUE INDEX uq_appointment_slots_day_start
    ON appointment_slots(consultation_day_id, slot_start);

CREATE UNIQUE INDEX uq_booking_campaigns_one_published
    ON booking_campaigns(status)
    WHERE status = 'PUBLISHED';
