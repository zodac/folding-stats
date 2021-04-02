CREATE TABLE hardware_categories (
    hardware_id SERIAL PRIMARY KEY,
    category_name TEXT NOT NULL,
    multiplier NUMERIC NOT NULL
);

CREATE INDEX index_hardware_id
    ON hardware_categories(hardware_id);


CREATE TABLE folding_users (
    user_id SERIAL PRIMARY KEY,
    folding_username TEXT NOT NULL,
    display_username TEXT NOT NULL,
    passkey TEXT NOT NULL,
    hardware_id INT NOT NULL,
    hardware_name TEXT NOT NULL,
    CONSTRAINT fk_hardware_id
        FOREIGN KEY(hardware_id)
            REFERENCES hardware_categories(hardware_id)
);

CREATE INDEX index_folding_user_id
    ON folding_users(user_id);


CREATE TABLE tc_teams (
    team_id SERIAL PRIMARY KEY,
    team_name TEXT NOT NULL,
    user_one INT NOT NULL,
    user_two INT NOT NULL,
    user_three INT NOT NULL,
    user_four INT NOT NULL,
    user_five INT NOT NULL,
    CONSTRAINT fk_user_one
        FOREIGN KEY(user_one)
            REFERENCES folding_users(user_id),
    CONSTRAINT fk_user_two
        FOREIGN KEY(user_two)
            REFERENCES folding_users(user_id),
    CONSTRAINT fk_user_three
        FOREIGN KEY(user_three)
            REFERENCES folding_users(user_id),
    CONSTRAINT fk_user_four
        FOREIGN KEY(user_four)
            REFERENCES folding_users(user_id),
    CONSTRAINT fk_user_five
        FOREIGN KEY(user_five)
            REFERENCES folding_users(user_id)
);

CREATE INDEX index_tc_team_id
    ON tc_teams(team_id);

CREATE TABLE individual_points (
    user_id INT,
    utc_timestamp TIMESTAMP,
    total_points BIGINT NOT NULL,
    PRIMARY KEY(user_id, utc_timestamp),
    CONSTRAINT fk_user_id
        FOREIGN KEY(user_id)
            REFERENCES folding_users(user_id)
)