SET SCHEMA 'partnerschema';


ALTER TABLE IF EXISTS partnerdb.partnerschema.product ADD COLUMN IF NOT EXISTS extension_limit INTEGER NOT NULL DEFAULT 0 ;
COMMENT ON COLUMN product.extension_limit is 'Number of PLM extensions allowed';

ALTER TABLE IF EXISTS partnerdb.partnerschema.manifest DROP COLUMN IF EXISTS extension_count;