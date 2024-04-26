SET SCHEMA 'partnerschema';

ALTER TABLE IF EXISTS product ADD COLUMN IF NOT EXISTS credential jsonb null;
COMMENT ON COLUMN product.credential is 'Partner credential schema';
