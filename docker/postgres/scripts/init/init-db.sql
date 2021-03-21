CREATE TABLE IF NOT EXISTS devices (
    device_id TEXT PRIMARY KEY,
    timestamp_utc INTEGER NOT NULL,
    latitude NUMERIC NOT NULL,
    longitude NUMERIC NOT NULL
);

CREATE INDEX index_device_id
    ON devices(device_id);
