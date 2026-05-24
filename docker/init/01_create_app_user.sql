CREATE USER common_ground_user WITH PASSWORD 'common_ground_password';
GRANT CONNECT ON DATABASE common_ground TO common_ground_user;
-- Required for Flyway to create the common_ground schema on first run
GRANT CREATE ON DATABASE common_ground TO common_ground_user;
-- Prevent access to the public schema
REVOKE ALL ON SCHEMA public FROM common_ground_user;
