SET SCHEMA 'partnerschema';

ALTER TABLE IF EXISTS product ADD COLUMN IF NOT EXISTS feature JSONB NOT NULL DEFAULT '{"receiveAutomatedTransactionUpdates": false}'::jsonb;
COMMENT ON COLUMN product.feature is 'Features available in product';