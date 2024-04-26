SET SCHEMA 'partnerschema';

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