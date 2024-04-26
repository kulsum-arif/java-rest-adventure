SET SCHEMA 'partnerschema';

CREATE INDEX IF NOT EXISTS idxgin_product_request_types ON partnerschema.product USING gin (request_types);
