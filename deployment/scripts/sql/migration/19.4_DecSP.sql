SET SCHEMA 'partnerschema';

-- Update credential property type to 'string', 'boolean' and 'number'
update partnerschema.product set credential=(replace(replace(replace(credential::text, 'String', 'string'),'Boolean', 'boolean'),'Numeric', 'number'))::jsonb where credential is not null;
