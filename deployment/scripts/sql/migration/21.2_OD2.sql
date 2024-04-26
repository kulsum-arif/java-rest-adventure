SET SCHEMA 'partnerschema';

UPDATE product
SET tags = jsonb_set(
        tags::jsonb,
        '{categories}',
        upper(tags->>'categories')::jsonb,
        false
    )
where status <> 'deprecated'
    and (tags->>'categories') != upper(tags->>'categories')
    and partner_id not like '0070%';

UPDATE product
SET tags = jsonb_set(
        tags::jsonb,
        '{categories}',
        replace(
            tags->>'categories',
            '"CLOSINGFEES"',
            '"CLOSINGFEES","ClosingFees"'
        )::jsonb,
        false
    )
where tags->>'categories' like '%CLOSINGFEES%'
    and status <> 'deprecated'
    and partner_id not like '%0070%'; 