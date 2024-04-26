-- Add application and order_type columns
SET SCHEMA 'partnerschema';

-- Rename created_at and updated_at columns
DROP TABLE IF EXISTS partnerschema.manifest CASCADE;

-- Table for product details
CREATE TABLE IF NOT EXISTS product (
    id UUID NOT NULL,
    partner_id CHARACTER varying(64) NOT NULL,
    name CHARACTER varying(64) NOT NULL,
    listing_name CHARACTER varying(256) NOT NULL,
    environment CHARACTER varying(64) NOT NULL,
    status CHARACTER varying(32) NOT NULL,
    interface_url CHARACTER varying(512),
    integration_type CHARACTER varying(20) NOT NULL,
    partner_url CHARACTER varying(512),
    request_types jsonb,
    tags jsonb,
    webhook_subscriptions jsonb,
    access_entitlements jsonb,
    created_by CHARACTER varying(128) NOT NULL,
    created TIMESTAMP WITH TIME zone NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_by CHARACTER varying(128),
    updated TIMESTAMP WITH TIME zone,
    version_number INTEGER NOT NULL,
        CONSTRAINT pk_product_id PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS product_idx_partnerid_productname_u1 ON partnerschema.product (partner_id, name);

-- Table Comments for product
comment on table product is 'Partner product details';
comment on column product.id is 'Unique identifier of the partner product';
comment on column product.partner_id is 'Unique identifier of the partner';
comment on column product.name is 'Product name';
comment on column product.listing_name is 'Product Listing Name ';
comment on column product.environment is 'Partner environment';
comment on column product.status is 'Product status';
comment on column product.interface_url is 'Partner interface url';
comment on column product.integration_type is 'Partner integration type such as P2P and ASYNC';
comment on column product.partner_url is 'Partner url for P2P integration';
comment on column product.request_types is 'Product request types';
comment on column product.tags is 'Search tags in key value pair';
comment on column product.webhook_subscriptions is 'List of webhook subcription ids';
comment on column product.access_entitlements is 'Access Entitlement';
comment on column product.created_by is 'User who created the row';
comment on column product.created is 'Date and time of the creation of the row';
comment on column product.updated_by is 'User who last updated the row';
comment on column product.updated is 'Date and time of the last update of the row';
comment on column product.version_number is 'Version number for optimistic lock';

comment on index product_idx_partnerid_productname_u1 is 'Unique index on partner_id, name';

-- Table for manifest
CREATE TABLE IF NOT EXISTS manifest (
    id UUID NOT NULL,
    product_id UUID NOT NULL,
    origin jsonb,
    request jsonb,
    response jsonb,
    created_by CHARACTER varying(128) NOT NULL,
    created TIMESTAMP WITH TIME zone NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_by CHARACTER varying(128),
    updated TIMESTAMP WITH TIME zone,
    version_number INTEGER NOT NULL,
        CONSTRAINT pk_manifest_id PRIMARY KEY (id),
        CONSTRAINT fk_product_id FOREIGN KEY (product_id) REFERENCES partnerschema.product (id)
);


-- Table Comments for Provider
comment on table manifest is 'Partner manifest details';
comment on column manifest.id is 'Unique identifier of the partner manifest';
comment on column manifest.product_id is 'Unique identifier of the product';
comment on column manifest.origin is 'Origin manifest in json format';
comment on column manifest.request is 'Request manifest json format';
comment on column manifest.response is 'Response manifest json format';
comment on column manifest.created_by is 'User who created the row';
comment on column manifest.created is 'Date and time of the creation of the row';
comment on column manifest.updated_by is 'User who last updated the row';
comment on column manifest.updated is 'Date and time of the last update of the row';
comment on column manifest.version_number is 'Version number for optimistic lock';

comment on index pk_manifest_id is 'Unique index id';