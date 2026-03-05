-- ============================================
-- PostgreSQL Initialization Script
-- ============================================
-- This script runs automatically when postgres container initializes for the first time
-- Note: This script only runs when postgres_data volume is empty (first run)
-- For existing volumes, schema is created by SchemaInitializer in the Spring app

-- Create schema for application
CREATE SCHEMA IF NOT EXISTS laundry_locker_schema;

-- Grant privileges to postgres user on schema
GRANT ALL PRIVILEGES ON SCHEMA laundry_locker_schema TO postgres;

-- Set default search path for the database
ALTER DATABASE laundry_locker SET search_path TO laundry_locker_schema, public;

-- Also set search path for current session
SET search_path TO laundry_locker_schema, public;

-- Completion notification
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Database initialization completed!';
    RAISE NOTICE 'Schema: laundry_locker_schema';
    RAISE NOTICE 'Sample data will be loaded by Spring Boot SampleDataLoader';
    RAISE NOTICE '========================================';
END $$;

