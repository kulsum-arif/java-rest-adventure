-- Create Readonly partner db user
CREATE USER partner_readonly WITH PASSWORD 'password123';
GRANT SELECT ON ALL TABLES IN SCHEMA partnerschema TO partner_readonly;
GRANT USAGE ON SCHEMA partnerschema TO partner_readonly;
