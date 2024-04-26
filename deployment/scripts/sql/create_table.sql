SET SCHEMA 'partnerschema';
SET default_tablespace = 'pg_default';
-- Table for product details
CREATE TABLE IF NOT EXISTS product (
    id UUID NOT NULL,
    partner_id CHARACTER varying(64) NOT NULL,
    name CHARACTER varying(64) NOT NULL,
    listing_name CHARACTER varying(256) NOT NULL,
    environment CHARACTER varying(64) NOT NULL,
    status CHARACTER varying(32) NOT NULL,
    interface_url CHARACTER varying(512),
    admin_interface_url CHARACTER varying(512),
    integration_type CHARACTER varying(20) NOT NULL,
    partner_url CHARACTER varying(512),
    extension_limit INTEGER NOT NULL,
    request_types jsonb,
    tags jsonb,
    webhook_subscriptions jsonb,
    access_entitlements jsonb,
    credentials jsonb,
    options jsonb,
    additional_links jsonb,
    created_by CHARACTER varying(128) NOT NULL,
    created TIMESTAMP WITH TIME zone NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_by CHARACTER varying(128),
    updated TIMESTAMP WITH TIME zone,
    version_number INTEGER NOT NULL,
    feature JSONB NOT NULL DEFAULT '{"receiveAutomatedTransactionUpdates": false}'::jsonb;
        CONSTRAINT pk_product_id PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS product_idx_partnerid_productname_u1 ON partnerschema.product (partner_id, name);
CREATE INDEX IF NOT EXISTS idxgin_product_tags ON product USING gin (tags);
CREATE INDEX IF NOT EXISTS idx_product_environment ON product (environment);
CREATE INDEX IF NOT EXISTS idxgin_product_request_types ON partnerschema.product USING gin (request_types);

-- Table Comments for product
comment on table product is 'Partner product details';
comment on column product.id is 'Unique identifier of the partner product';
comment on column product.partner_id is 'Unique identifier of the partner';
comment on column product.name is 'Product name';
comment on column product.listing_name is 'Product Listing Name ';
comment on column product.environment is 'Partner environment';
comment on column product.status is 'Product status';
comment on column product.interface_url is 'Partner interface url';
comment on column product.admin_interface_url is 'Partner admin interface url';
comment on column product.integration_type is 'Partner integration type such as P2P and ASYNC';
comment on column product.partner_url is 'Partner url for P2P integration';
comment on column product.request_types is 'Product request types';
comment on column product.tags is 'Search tags in key value pair';
comment on column product.webhook_subscriptions is 'List of webhook subcription ids';
comment on column product.access_entitlements is 'Access Entitlement';
comment ON column product.credentials is 'Partner credential schema';
comment ON column product.options is 'Options schema for requestTypes';
comment on column product.created_by is 'User who created the row';
comment on column product.created is 'Date and time of the creation of the row';
comment on column product.updated_by is 'User who last updated the row';
comment on column product.updated is 'Date and time of the last update of the row';
comment on column product.version_number is 'Version number for optimistic lock';
comment ON column product.feature is 'Features available in product';
comment on index product_idx_partnerid_productname_u1 is 'Unique index on partner_id, name';
comment on index idxgin_product_tags is 'jsonb index for tags';
comment on index idx_product_environment is 'Index on product environment';
comment on index idxgin_product_request_types is 'jsonb index for request_types';

-- Table for manifest
CREATE TABLE IF NOT EXISTS manifest (
    id UUID NOT NULL,
    product_id UUID NOT NULL,
    origin jsonb,
    transactions jsonb,
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
comment on column manifest.origin is 'Origin entitlement in json format';
comment on column manifest.transactions is 'Transaction entitlements json format';
comment on column manifest.created_by is 'User who created the row';
comment on column manifest.created is 'Date and time of the creation of the row';
comment on column manifest.updated_by is 'User who last updated the row';
comment on column manifest.updated is 'Date and time of the last update of the row';
comment on column manifest.version_number is 'Version number for optimistic lock';
comment on column manifest.extension_count is 'Number of PLM extensions allowed';

comment on index pk_manifest_id is 'Unique index id';

-- billing engine tables

CREATE TABLE IF NOT EXISTS billing_rules (
	  id uuid NOT NULL,
	  product_id uuid NOT NULL,
	  status VARCHAR(64) NOT NULL,
	  schema_version VARCHAR(64) NULL,
	  transformations jsonb,
	  created_by CHARACTER varying(128) NOT NULL,
    created TIMESTAMP WITH TIME zone NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_by CHARACTER varying(128),
    updated TIMESTAMP WITH TIME zone,
	  version_number INTEGER NOT NULL,
	      CONSTRAINT pk_billing_rule_id PRIMARY KEY (id),
	      CONSTRAINT fk_billing_product_id FOREIGN KEY (product_id) REFERENCES partnerschema.product (id)
);
CREATE INDEX idx_billing_rule_product_id ON billing_rules USING HASH (product_id);
COMMENT ON INDEX idx_billing_rule_product_id IS 'Index on product_id';

-- Table Comments for billing_rules
comment on table billing_rules is 'Product billing rules details';
comment on column billing_rules.id is 'Unique identifier of the Billing rules';
comment on column billing_rules.product_id is 'Unique identifier of the Product';
comment on column billing_rules.status is 'Determines the status of the current billing rules record';
comment on column billing_rules.schema_version is 'Determines the status of the current billing rule schema';
comment on column billing_rules.transformations is 'Contains the list of transforms defined for a given billing rule';
comment on column billing_rules.created_by is 'User who created the row';
comment on column billing_rules.created is 'Date and time of the creation of the row';
comment on column billing_rules.updated_by is 'User who last updated the row';
comment on column billing_rules.updated is 'Date and time of the last update of the row';
comment on column billing_rules.version_number is 'Version number for optimistic lock';


CREATE TABLE IF NOT EXISTS billing_rules_change_log (
    changeset_id INTEGER NOT NULL,
	  billing_rule_id uuid NOT NULL,
	  operation VARCHAR(64) NOT NULL,
	  content jsonb,
    comments VARCHAR NULL,
	  created_by CHARACTER varying(128) NOT NULL,
    created TIMESTAMP WITH TIME zone NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_by CHARACTER varying(128),
    updated TIMESTAMP WITH TIME zone,
	  version_number INTEGER NOT NULL,
	      CONSTRAINT pk_changelog_changeset_id_billing_rule_id PRIMARY KEY (changeset_id, billing_rule_id),
        CONSTRAINT fk_changelog_billing_rule_id FOREIGN KEY (billing_rule_id) REFERENCES partnerschema.billing_rules (id)

);
CREATE INDEX IF NOT EXISTS idx_changelog_changeset_id_billing_rule_id ON billing_rules_change_log (changeset_id,billing_rule_id);

COMMENT ON INDEX partnerschema.idx_changelog_changeset_id_billing_rule_id IS 'Index on billing_rule_id';

-- Table Comments for billing_rules_change_log
comment on table billing_rules_change_log is 'Product billing rules change log details';
comment on column billing_rules_change_log.billing_rule_id is 'Unique identifier of the Billing rule';
comment on column billing_rules_change_log.changeset_id is 'Refers to the change set version of the billing rule definition';
comment on column billing_rules_change_log.operation is 'Determines the action taken on the current billing rule';
comment on column billing_rules_change_log.content is 'Contains the content that was modified';
comment on column billing_rules_change_log.comments is 'Contains the comments added as part of the action';
comment on column billing_rules_change_log.created_by is 'User who created the row';
comment on column billing_rules_change_log.created is 'Date and time of the creation of the row';
comment on column billing_rules_change_log.updated_by is 'User who last updated the row';
comment on column billing_rules_change_log.updated is 'Date and time of the last update of the row';
comment on column billing_rules_change_log.version_number is 'Version number for optimistic lock';