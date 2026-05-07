CREATE TABLE booking_campaigns (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    booking_start_date DATE NOT NULL,
    booking_end_date DATE NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT ck_booking_campaigns_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED')),
    CONSTRAINT ck_booking_campaigns_dates CHECK (booking_start_date <= booking_end_date)
);

CREATE TABLE consultation_days (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    consultation_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    lunch_start TIME NULL,
    lunch_end TIME NULL,
    slot_duration_minutes INT NOT NULL,
    CONSTRAINT fk_consultation_days_campaign
        FOREIGN KEY (campaign_id) REFERENCES booking_campaigns(id),
    CONSTRAINT ck_consultation_days_time CHECK (start_time < end_time),
    CONSTRAINT ck_consultation_days_lunch CHECK (
        (lunch_start IS NULL AND lunch_end IS NULL)
        OR (lunch_start IS NOT NULL AND lunch_end IS NOT NULL AND lunch_start < lunch_end)
    ),
    CONSTRAINT ck_consultation_days_slot_duration CHECK (slot_duration_minutes > 0)
);

CREATE TABLE appointment_slots (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    consultation_day_id BIGINT NOT NULL,
    slot_start DATETIME2 NOT NULL,
    slot_end DATETIME2 NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_appointment_slots_consultation_day
        FOREIGN KEY (consultation_day_id) REFERENCES consultation_days(id),
    CONSTRAINT ck_appointment_slots_status CHECK (status IN ('AVAILABLE', 'BOOKED', 'BLOCKED')),
    CONSTRAINT ck_appointment_slots_time CHECK (slot_start < slot_end)
);

CREATE UNIQUE INDEX uq_appointment_slots_start ON appointment_slots(slot_start);

CREATE TABLE appointments (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    slot_id BIGINT NOT NULL,
    employee_number NVARCHAR(50) NOT NULL,
    full_name NVARCHAR(150) NOT NULL,
    email NVARCHAR(150) NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    cancel_token UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_appointments_slot
        FOREIGN KEY (slot_id) REFERENCES appointment_slots(id),
    CONSTRAINT ck_appointments_status CHECK (status IN ('ACTIVE', 'CANCELED'))
);

CREATE UNIQUE INDEX uq_appointments_slot_active
    ON appointments(slot_id)
    WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX uq_appointments_employee_active
    ON appointments(employee_number)
    WHERE status = 'ACTIVE';

CREATE TABLE notification_log (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    notification_type NVARCHAR(30) NOT NULL,
    recipient_email NVARCHAR(150) NOT NULL,
    status NVARCHAR(20) NOT NULL,
    error_message NVARCHAR(1000) NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_notification_log_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    CONSTRAINT ck_notification_log_type CHECK (notification_type IN ('CREATED', 'UPDATED', 'CANCELED')),
    CONSTRAINT ck_notification_log_status CHECK (status IN ('SENT', 'FAILED', 'SKIPPED'))
);
