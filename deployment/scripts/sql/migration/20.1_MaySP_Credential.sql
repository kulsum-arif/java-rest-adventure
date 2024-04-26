SET SCHEMA 'partnerschema';

ALTER TABLE IF EXISTS partnerdb.partnerschema.product ADD COLUMN IF NOT EXISTS credentials jsonb null;
COMMENT ON COLUMN product.credentials is 'Partner credential schema';

UPDATE partnerdb.partnerschema.product SET credentials = '[
  {
    "id": "appKey",
    "type": "string",
    "scope": "user",
    "title": "Application Key",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": "^(.*)$",
    "required": true
  },
  {
    "id": "partnerId",
    "type": "string",
    "scope": "user",
    "title": "Partner Id",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": "^(.*)$",
    "required": true
  },
  {
    "id": "partnerSecret",
    "type": "string",
    "scope": "user",
    "title": "Partner Secret",
    "secret": true,
    "maximum": 0,
    "minimum": 0,
    "pattern": "^(.*)$",
    "required": true
  }
]' where partner_id='007001' AND name='finicity-services';

UPDATE partnerdb.partnerschema.product SET credentials = '[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "maximum": 30,
    "minimum": 3,
    "pattern": null,
    "required": true
  },
  {
    "id": "password",
    "type": "string",
    "scope": "user",
    "title": "Password",
    "secret": true,
    "maximum": 20,
    "minimum": 2,
    "pattern": "[a-zA-Z0-9_]",
    "required": true
  },
  {
    "id": "branchId",
    "type": "string",
    "scope": "company",
    "title": "Branch Id",
    "secret": false,
    "maximum": 500,
    "minimum": 0,
    "pattern": null,
    "required": false
  }
]' WHERE partner_id='007001' AND name='01-Flood-DataVerifyCBC';


UPDATE partnerdb.partnerschema.product SET credentials = '[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required":true
  },
  {
    "id": "password",
    "type": "string",
    "scope": "user",
    "title": "Password",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required":true
  },
  {
    "id": "orgID",
    "type": "string",
    "scope": "company",
    "title": "orgID",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required":false
  }
]' WHERE partner_id='007001' AND name='A_QA_TEST_SSF_Ashwani_Product';

UPDATE partnerdb.partnerschema.product set credentials='[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required": false
  },
  {
    "id": "password",
    "type": "string",
    "scope": "user",
    "title": "Password",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required": false
  },
  {
    "id": "orgID",
    "type": "string",
    "scope": "company",
    "title": "orgID",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required": true
  }
]' WHERE partner_id='007001' AND name='ssf-showcase21';

UPDATE partnerdb.partnerschema.product set credentials='[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required": true
  },
  {
    "id": "password",
    "type": "string",
    "scope": "user",
    "title": "Password",
    "secret": true,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required": true
  },
  {
    "id": "companyId",
    "type": "string",
    "scope": "company",
    "title": "Company ID",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required": false
  }
]' WHERE partner_id='007001' AND name='ssf-showcase';


UPDATE partnerdb.partnerschema.product set credentials='[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required": true
  },
  {
    "id": "password",
    "type": "string",
    "scope": "user",
    "title": "Password",
    "secret": true,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required": true
  },
  {
    "id": "companyId",
    "type": "string",
    "scope": "company",
    "title": "Company ID",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required": true
  }
]' WHERE partner_id='007001' AND name='flood-SLP-test-product';

UPDATE partnerdb.partnerschema.product set credentials='[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required": true
  },
  {
    "id": "password",
    "type": "string",
    "scope": "user",
    "title": "Password",
    "secret": true,
    "maximum": 0,
    "minimum": 0,
    "pattern": null,
    "required": true
  }
]' WHERE partner_id='007001' AND name='form-free-services';

UPDATE partnerdb.partnerschema.product set credentials='[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "maximum": 30,
    "minimum": 3,
    "pattern": null,
    "required": true
  },
  {
    "id": "password",
    "type": "string",
    "scope": "user",
    "title": "Password",
    "secret": true,
    "maximum": 20,
    "minimum": 2,
    "pattern": "[a-zA-Z0-9_]",
    "required": true
  },
  {
    "id": "branchId",
    "type": "string",
    "scope": "company",
    "title": "Branch Id",
    "secret": false,
    "maximum": 500,
    "minimum": 0,
    "pattern": null,
    "required": false
  }
]' WHERE partner_id='007001' AND name='02-Flood-DataVerifyCBC';

UPDATE partnerdb.partnerschema.product set credentials='[
  {
    "id": "appKey",
    "type": "string",
    "title": "Application Key",
    "pattern": "^(.*)$",
    "minimum": 0,
    "maximum": 0,
    "secret": false,
    "scope": "user",
    "required": true
  },
  {
    "id": "partnerId",
    "type": "string",
    "title": "Partner Id",
    "pattern": "^(.*)$",
    "minimum": 0,
    "maximum": 0,
    "secret": false,
    "scope": "user",
    "required": true
  },
  {
    "id": "partnerSecret",
    "type": "string",
    "title": "Partner Secret",
    "pattern": "^(.*)$",
    "minimum": 0,
    "maximum": 0,
    "secret": true,
    "scope": "user",
    "required": true
  }
]' WHERE partner_id='007001' AND name IN ('finicity-voe-services','finicity-voa-services');

UPDATE partnerdb.partnerschema.product SET credentials = '[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "pattern": "^(.*)$",
    "required": true
  },
  {
    "id": "accountId",
    "type": "string",
    "scope": "user",
    "title": "AccountId",
    "secret": false,
    "pattern": "^(.*)$",
    "required": true
  }
]' WHERE partner_id='11114326' AND name='ProductandPricingOptimalBlue';

UPDATE partnerdb.partnerschema.product SET credentials = '[
  {
    "id": "username",
    "type": "string",
    "scope": "company",
    "title": "Username",
    "secret": false,
    "required": true
  },
  {
    "id": "password",
    "type": "string",
    "scope": "company",
    "title": "Password",
    "secret": true,
    "required": true
  }
]' where partner_id='11171107' AND name='VOAFormFreePOC';

UPDATE partnerdb.partnerschema.product SET credentials = '[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "required": true
  },
  {
    "id": "password",
    "type": "string",
    "scope": "user",
    "title": "Password",
    "secret": false,
    "required": true
  }
]' WHERE partner_id='11158557' AND name='ob_test_product_qa';

UPDATE partnerdb.partnerschema.product SET credentials = '[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "required": true
  },
  {
    "id": "password",
    "type": "string",
    "scope": "user",
    "title": "Password",
    "secret": false,
    "required": true
  }
]' WHERE partner_id='11158557' AND name='ob_test_product';

UPDATE partnerdb.partnerschema.product SET credentials = '[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "required": true
  },
  {
    "id": "password",
    "type": "string",
    "scope": "user",
    "title": "Password",
    "secret": true,
    "required": true
  },
  {
    "id": "service-username",
    "type": "string",
    "scope": "company",
    "title": "Service Account",
    "secret": false,
    "required": true
  },
  {
    "id": "service-password",
    "type": "string",
    "scope": "company",
    "title": "Service Password",
    "secret": true,
    "required": true
  }
]' WHERE partner_id='11158998' AND name='ClosingFeesSmartFees-EPCV2';

UPDATE partnerdb.partnerschema.product SET credentials = '[
  {
    "id": "username",
    "type": "string",
    "scope": "user",
    "title": "Username",
    "secret": false,
    "required": true
  },
  {
    "id": "accountId",
    "type": "string",
    "scope": "company",
    "title": "Account ID",
    "secret": false,
    "required": true
  },
  {
    "id": "password",
    "type": "string",
    "scope": "user",
    "title": "Password",
    "secret": true,
    "required": false
  }
]' WHERE partner_id='11171107' AND name='AppraisalMCLemValuation';

ALTER TABLE IF EXISTS partnerdb.partnerschema.product DROP COLUMN IF EXISTS credential;