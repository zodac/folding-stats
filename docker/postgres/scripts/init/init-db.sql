CREATE TABLE hardware_categories (
    hardware_id SERIAL PRIMARY KEY,
    category_name TEXT NOT NULL,
    multiplier NUMERIC NOT NULL
);

CREATE INDEX index_hardware_id
    ON hardware_categories(hardware_id);


CREATE TABLE tc_users (
    user_id SERIAL PRIMARY KEY,
    user_name TEXT NOT NULL,
    team_position TEXT NOT NULL,
    hardware_id INT NOT NULL,
    hardware_name TEXT NOT NULL,
    FOREIGN KEY (hardware_id) hardware_categories(hardware_id)
);

CREATE INDEX index_tc_user_id
    ON tc_users(user_id);


CREATE TABLE tc_teams (
    team_id SERIAL PRIMARY KEY,
    team_name TEXT NOT NULL,
    user_one INT NOT NULL,
    user_two INT NOT NULL,
    user_three INT NOT NULL,
    user_four INT NOT NULL,
    user_five INT NOT NULL,
    FOREIGN KEY (user_one) tc_users(user_id),
    FOREIGN KEY (user_two) tc_users(user_id),
    FOREIGN KEY (user_three) tc_users(user_id),
    FOREIGN KEY (user_four) tc_users(user_id),
    FOREIGN KEY (user_five) tc_users(user_id)
);

CREATE INDEX index_tc_team_id
    ON tc_teams(team_id);