SET SCHEMA 'partnerschema';

UPDATE partnerdb.partnerschema.manifest
SET origin = REPLACE(origin::text, '{}','{"fields": []}')::jsonb
from  partnerdb.partnerschema.product p
where p.id = product_id and cast(origin as text) ~ '{\s*}';

UPDATE partnerdb.partnerschema.manifest
SET transactions = REPLACE(transactions::text, '"request": {}', '"request": {"fields": []}')::jsonb
from  partnerdb.partnerschema.product p
where p.id = product_id and cast(transactions as text) ~ '"request": {\s*}';

UPDATE partnerdb.partnerschema.manifest
SET transactions = REPLACE(transactions::text, '"response": {}', '"response": {"fields": []}')::jsonb
from  partnerdb.partnerschema.product p
where p.id = product_id and cast(transactions as text) ~ '"response": {\s*}';