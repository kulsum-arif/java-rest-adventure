SET SCHEMA 'partnerschema';

ALTER TABLE IF EXISTS product ADD COLUMN IF NOT EXISTS additional_links JSONB NULL;
COMMENT ON COLUMN product.additional_links is 'Additional links supported by the product';