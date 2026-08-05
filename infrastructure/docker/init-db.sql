-- Create databases for all core platform microservices
CREATE DATABASE identity_db;
CREATE DATABASE org_db;
CREATE DATABASE billing_db;
CREATE DATABASE storage_db;
CREATE DATABASE notify_db;
CREATE DATABASE apikey_db;

-- Grant all privileges to the default postgres user
GRANT ALL PRIVILEGES ON DATABASE identity_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE org_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE billing_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE storage_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE notify_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE apikey_db TO postgres;
