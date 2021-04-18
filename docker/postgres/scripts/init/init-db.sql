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


CREATE TABLE tc_user_points (
    user_id INT,
    utc_timestamp TIMESTAMP,
    total_points BIGINT NOT NULL,
    total_unmultiplied_points BIGINT NOT NULL,
    total_units INT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)
);

CREATE INDEX index_tc_user_points
    ON tc_user_points(user_id, utc_timestamp);
