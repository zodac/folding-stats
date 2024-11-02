-- Enable the pgcrypto extension
CREATE EXTENSION pgcrypto;

-- System users who can update/create/delete
CREATE TABLE system_users (
    user_name TEXT NOT NULL PRIMARY KEY,
    user_password_hash TEXT NOT NULL,
    roles TEXT[] DEFAULT '{}'
);

INSERT INTO system_users (user_name, user_password_hash, roles)
VALUES
('%ADMIN_USERNAME%', crypt('%ADMIN_PASSWORD%', gen_salt('bf')), ARRAY ['admin']),
('%READ_ONLY_USERNAME%', crypt('%READ_ONLY_PASSWORD%', gen_salt('bf')), ARRAY ['read-only']);
