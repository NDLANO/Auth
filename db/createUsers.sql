-- Schema
CREATE SCHEMA auth;

-- WRITE
CREATE USER auth_write with PASSWORD '<passord>';

GRANT CONNECT ON DATABASE data_prod to auth_write;
GRANT USAGE ON SCHEMA auth to auth_write;
GRANT CREATE ON SCHEMA auth to auth_write;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA auth TO auth_write;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA auth TO auth_write;