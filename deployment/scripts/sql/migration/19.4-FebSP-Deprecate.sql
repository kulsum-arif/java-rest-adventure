SET SCHEMA 'partnerschema';
-- Update status and access entitlements
UPDATE partnerschema.product
SET status = 'deprecated', access_entitlements = '{"allow": "^$", "deny": "^$"}'
WHERE partner_id in ('007001','007002','007003','007004','007005','007006','007007','007008','007009');
