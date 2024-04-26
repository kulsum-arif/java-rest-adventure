SET SCHEMA 'partnerschema';

ALTER TABLE IF EXISTS manifest ADD COLUMN IF NOT EXISTS findings JSONB NULL;
COMMENT ON COLUMN manifest.findings is 'Findings supported by the product';

ALTER TABLE IF EXISTS manifest ADD COLUMN IF NOT EXISTS service_events JSONB NULL;
COMMENT ON COLUMN manifest.service_events is 'Service Events supported by the product';