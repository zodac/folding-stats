CREATE TABLE hardware (
    hardware_id SERIAL PRIMARY KEY,
    hardware_name TEXT NOT NULL,
    display_name TEXT NOT NULL,
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
        ON DELETE CASCADE
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
    total_wus INT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
        REFERENCES folding_users(user_id)
        ON DELETE CASCADE
)

-- TODO: [zodac] Add an index for the points table