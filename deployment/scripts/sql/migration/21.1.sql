SET SCHEMA 'partnerschema';

ALTER TABLE IF EXISTS partnerdb.partnerschema.product ADD COLUMN IF NOT EXISTS options jsonb null;
COMMENT ON COLUMN product.options is 'Options schema for requestTypes';