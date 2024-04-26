SET SCHEMA 'partnerschema';

select p.id, m.origin, m.transactions
from partnerdb.partnerschema.product p, partnerdb.partnerschema.manifest m
where p.id = m.product_id and (cast(origin as text) ~ '{\s*}' or cast(transactions as text) ~ '"request": {\s*}' or cast(transactions as text) ~ '"response": {\s*}');