CREATE TABLE hardware (
    hardware_id SERIAL PRIMARY KEY,
    hardware_name TEXT NOT NULL,
    display_name TEXT NOT NULL,
    operating_system TEXT NOT NULL,
    multiplier NUMERIC NOT NULL
);

CREATE INDEX index_hardware_id
    ON hardware(hardware_id);


CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    folding_username TEXT NOT NULL,
    display_username TEXT NOT NULL,
    passkey TEXT NOT NULL,
    category TEXT NOT NULL,
    hardware_id INT NOT NULL,
    folding_team_number INT NOT NULL,
    live_stats_link TEXT NULL,
    points_offset BIGINT DEFAULT(0),
    units_offset INT DEFAULT(0),
    CONSTRAINT uni_user UNIQUE(folding_username, passkey),
    CONSTRAINT fk_hardware_id
        FOREIGN KEY(hardware_id)
        REFERENCES hardware(hardware_id)
);

CREATE INDEX index_user_id
    ON users(user_id);


CREATE TABLE teams (
    team_id SERIAL PRIMARY KEY,
    team_name TEXT NOT NULL UNIQUE,
    team_description TEXT,
    captain_user_id INT NOT NULL,
    user_ids INT[] NOT NULL,
    CONSTRAINT fk_captain_user_id
        FOREIGN KEY(captain_user_id)
        REFERENCES users(user_id)
);

CREATE INDEX index_team_id
    ON teams(team_id);


-- Table which is populated with latest stats of a user when first added
-- Also modified in the case where the user has their folding_username, team or passkey updated
CREATE TABLE user_initial_stats (
    user_id INT,
    utc_timestamp TIMESTAMP,
    initial_points BIGINT NOT NULL,
    initial_units INT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- Table which is populated each update with the latest stats of a user
CREATE TABLE user_total_stats (
    user_id INT,
    utc_timestamp TIMESTAMP,
    total_points BIGINT NOT NULL,
    total_units INT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);


-- Table which is populated each update with the latest stats of a user, as a running total for the TC (reset each month)
-- The total stats are offset by the initial values (and any manual offset for a user)
-- The unmultiplied_points are then multiplied by the hardware multiplier
CREATE TABLE user_tc_stats_hourly (
    user_id INT,
    utc_timestamp TIMESTAMP,
    tc_points BIGINT NOT NULL,
    tc_points_multiplied BIGINT NOT NULL,
    tc_units INT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);
