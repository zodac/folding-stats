CREATE TABLE hardware (
    hardware_id SERIAL PRIMARY KEY,
    hardware_name TEXT NOT NULL,
    display_name TEXT NOT NULL,
    operating_system TEXT NOT NULL,
    multiplier NUMERIC NOT NULL
);

CREATE INDEX index_hardware_id
    ON hardware(hardware_id);


CREATE TABLE folding_users (
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

CREATE INDEX index_folding_user_id
    ON folding_users(user_id);


CREATE TABLE folding_teams (
    team_id SERIAL PRIMARY KEY,
    team_name TEXT NOT NULL UNIQUE,
    team_description TEXT,
    captain_user_id INT NOT NULL,
    user_ids INT[] NOT NULL,
    CONSTRAINT fk_captain_user_id
        FOREIGN KEY(captain_user_id)
        REFERENCES folding_users(user_id)
);

CREATE INDEX index_tc_team_id
    ON folding_teams(team_id);

CREATE TABLE individual_tc_points (
    user_id INT,
    utc_timestamp TIMESTAMP,
    total_points BIGINT NOT NULL,
    total_units INT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES folding_users(user_id)
);

CREATE INDEX index_individual_tc_points
    ON individual_tc_points(user_id, utc_timestamp);


CREATE TABLE historic_stats_tc_user_daily (
    user_id INT,
    utc_timestamp TIMESTAMP,
    daily_points BIGINT NOT NULL,
    daily_units INT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES folding_users(user_id)
);

CREATE INDEX index_historic_stats_tc_user_daily
    ON individual_tc_points(user_id, utc_timestamp);

CREATE TABLE historic_stats_tc_team_daily (
    team_id INT,
    utc_timestamp TIMESTAMP,
    daily_team_points BIGINT NOT NULL,
    daily_team_units INT NOT NULL,
    PRIMARY KEY(team_id, utc_timestamp),
    CONSTRAINT fk_team_id
        FOREIGN KEY(team_id)
        REFERENCES folding_teams(team_id)
);

CREATE INDEX index_historic_stats_tc_team_daily
    ON individual_tc_points(user_id, utc_timestamp);

CREATE TABLE historic_stats_tc_team_monthly (
    team_id INT,
    utc_timestamp TIMESTAMP,
    monthly_team_points BIGINT NOT NULL,
    monthly_team_units INT NOT NULL,
    PRIMARY KEY(team_id, utc_timestamp),
    CONSTRAINT fk_team_id
        FOREIGN KEY(team_id)
        REFERENCES folding_teams(team_id)
);

CREATE INDEX index_historic_stats_tc_team_monthly
    ON individual_tc_points(user_id, utc_timestamp);