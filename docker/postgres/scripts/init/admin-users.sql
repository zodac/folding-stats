-- Enable the pgcrypto extension
CREATE EXTENSION pgcrypto;

-- System admin users who can update/create/delete
CREATE TABLE admin_users (
    user_name TEXT NOT NULL PRIMARY KEY,
    user_password_hash TEXT NOT NULL
);

INSERT INTO admin_users (user_name, user_password_hash)
VALUES ('ADMIN_USERNAME', crypt('ADMIN_PASSWORD', gen_salt('bf')));