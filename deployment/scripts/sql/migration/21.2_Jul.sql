SET SCHEMA 'partnerschema';

ALTER TABLE manifest DROP COLUMN IF EXISTS id;

ALTER TABLE manifest RENAME COLUMN product_id TO id;

ALTER TABLE manifest ADD constraint pk_manifest_id primary key (id);
