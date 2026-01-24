-- ============================================
-- PostgreSQL Initialization Script
-- ============================================
-- This script runs automatically when postgres container initializes for the first time

-- Create schema for application
CREATE SCHEMA IF NOT EXISTS laundry_locker_schema;

-- Grant privileges to postgres user on schema
GRANT ALL PRIVILEGES ON SCHEMA laundry_locker_schema TO postgres;

-- Set default search path
ALTER DATABASE laundry_locker SET search_path TO laundry_locker_schema, public;

-- Completion notification
DO $$
BEGIN
    RAISE NOTICE 'Database initialization completed successfully!';
    RAISE NOTICE 'Schema laundry_locker_schema has been created.';
    RAISE NOTICE 'Sample data will be loaded by Flyway migration V7__sample_data.sql';
END $$;

