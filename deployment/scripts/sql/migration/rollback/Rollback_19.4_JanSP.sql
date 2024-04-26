SET SCHEMA 'partnerschema';

-- Update access entitlements
UPDATE partnerschema.product
SET access_entitlements = '{"allow": [""], "deny": [""]}';
