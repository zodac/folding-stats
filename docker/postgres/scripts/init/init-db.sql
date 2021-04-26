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
    live_stats_link TEXT NULL,
    is_retired BOOLEAN DEFAULT(false),
    CONSTRAINT unique_user UNIQUE(folding_username, passkey),
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
    retired_user_ids INT[] DEFAULT '{}',
    CONSTRAINT fk_captain_user_id
        FOREIGN KEY(captain_user_id)
        REFERENCES users(user_id)
);

CREATE INDEX index_team_id
    ON teams(team_id);


-- Table which is populated with latest stats of a user when first added
-- Also modified in the case where the user has their folding_username, hardware, team or passkey updated
CREATE TABLE user_initial_stats (
    user_id INT NOT NULL,
    utc_timestamp TIMESTAMP NOT NULL,
    initial_points BIGINT NOT NULL,
    initial_units INT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE INDEX index_user_initial_stats
    ON user_initial_stats(user_id, utc_timestamp);


-- Table which is populated with manual offset stats of a user, to be added manually
CREATE TABLE user_offset_tc_stats (
    user_id INT UNIQUE NOT NULL,
    utc_timestamp TIMESTAMP NOT NULL,
    offset_multiplied_points BIGINT NOT NULL,
    offset_units INT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE INDEX index_user_offset_tc_stats
    ON user_offset_tc_stats(user_id, utc_timestamp);


-- Table which is populated each update with the latest stats of a user
CREATE TABLE user_total_stats (
    user_id INT NOT NULL,
    utc_timestamp TIMESTAMP NOT NULL,
    total_points BIGINT NOT NULL,
    total_units INT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE INDEX index_user_total_stats
    ON user_total_stats(user_id, utc_timestamp);


-- Table which is populated whenever a user is retired from a team
-- Do not cascade delete, as even if a user is deleted, a team may be referencing it for the rest of the month
CREATE TABLE retired_user_stats (
    id SERIAL,
    user_id INT NOT NULL,
    team_id INT NOT NULL,
    display_username TEXT NOT NULL,
    utc_timestamp TIMESTAMP NOT NULL,
    final_points BIGINT NOT NULL,
    final_multiplied_points BIGINT NOT NULL,
    final_units INT NOT NULL,
    PRIMARY KEY(user_id, team_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)
);

CREATE INDEX index_retired_user_stats
    ON retired_user_stats(team_id, user_id, utc_timestamp);

-- Table which is populated each update with the latest stats of a user, as a running total for the TC (reset each month)
-- The total stats are offset by the initial values (and any manual offset for a user)
-- The unmultiplied_points are then multiplied by the hardware multiplier
CREATE TABLE user_tc_stats_hourly (
    user_id INT NOT NULL,
    utc_timestamp TIMESTAMP NOT NULL,
    tc_points BIGINT NOT NULL,
    tc_points_multiplied BIGINT NOT NULL,
    tc_units INT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE INDEX index_user_tc_stats_hourly
    ON user_tc_stats_hourly(user_id, utc_timestamp);

