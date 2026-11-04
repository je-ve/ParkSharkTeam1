CREATE TABLE IF NOT EXISTS allocation
(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    license_plate_number VARCHAR(20) NOT NULL,
    member_id UUID NOT NULL REFERENCES members (id),
    parking_lot_id UUID NOT NULL REFERENCES parking_lot (id)

    CONSTRAINT chk_status_in_ongoing_or_stopped
    CHECK (status IN ('ONGOING','STOPPED'))

)