SET SCHEMA 'partnerschema';

-- Admin interface url changes
ALTER TABLE IF EXISTS product ADD COLUMN IF NOT EXISTS admin_interface_url CHARACTER varying(512);
COMMENT ON COLUMN product.admin_interface_url is 'Partner admin interface url';

-- Data Entitlement Changes
ALTER TABLE IF EXISTS manifest ADD COLUMN IF NOT EXISTS transactions jsonb null;
COMMENT ON COLUMN manifest.transactions is 'List of request and response data entitlements by request types';

--EPC-12791-Migrate existing data entitlements to new schema to support multiple request types
UPDATE manifest m1
SET "transactions" = concat('[{"requestTypes":',p.request_types,',"request":',m1.request,',"response":',m1.response,'}]')::jsonb,
updated = current_timestamp,
updated_by = 'urn:elli:service:epc-admin'
FROM  (SELECT product_id FROM manifest) m
INNER JOIN (SELECT id,request_types FROM product) p ON p.id = m.product_id
WHERE m1."transactions" IS NULL and m1.product_id = p.id;

ALTER TABLE IF EXISTS manifest DROP COLUMN IF EXISTS request;
ALTER TABLE IF EXISTS manifest DROP COLUMN IF EXISTS response;

-- Reset all access_entitlements for new format (rarora2)
UPDATE product SET access_entitlements = null;

-------------- Adding extension count column ----------------
-- 1. Add column
ALTER TABLE IF EXISTS manifest ADD COLUMN IF NOT EXISTS extension_count INTEGER ;
COMMENT ON COLUMN manifest.extension_count is 'Number of PLM extensions allowed';

-- 2. Initialize values
update MANIFEST set extension_count = 0 where extension_count is null;

-- 3. Add not null constraint
ALTER table if exists manifest ALTER COLUMN extension_count SET not null;

-- 4. Remove any existing tags
UPDATE product SET tags = NULL;

-- 5. Create GIN Index on tags
CREATE INDEX IF NOT EXISTS idxgin_product_tags ON product USING gin (tags);
CREATE INDEX IF NOT EXISTS idx_product_environment ON product (environment);
comment on index idxgin_product_tags is 'jsonb index for tags';
comment on index idx_product_environment is 'Index on product environment';