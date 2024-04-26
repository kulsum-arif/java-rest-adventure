package com.synkrato.services.partner;

import lombok.NoArgsConstructor;

/** This class has all the common constants used by the partner service */
@NoArgsConstructor
public class PartnerServiceConstants {

  public static final String MANIFEST_ORIGIN_TYPE = "origin";
  public static final String MANIFEST_REQUEST_TYPE = "request";
  public static final String MANIFEST_RESPONSE_TYPE = "response";
  public static final String PARTNER_SERVICE = "PartnerService";
  public static final String MANIFEST_ID_PATH_PARAM = "id";
  public static final String MANIFEST_ID_PATH_PARAM_MAPPING = "{" + MANIFEST_ID_PATH_PARAM + "}";
  public static final String MANIFEST_TYPE_PARAM = "manifestType";
  public static final String MANIFEST_UPDATE_URI =
      "/"
          + MANIFEST_ID_PATH_PARAM_MAPPING
          + "/{"
          + MANIFEST_TYPE_PARAM
          + ":"
          + MANIFEST_ORIGIN_TYPE
          + "|"
          + MANIFEST_REQUEST_TYPE
          + "|"
          + MANIFEST_RESPONSE_TYPE
          + "}";
  public static final String WEBHOOK_TOKEN_CACHE = "webhookTokenCache";
  public static final String S3_TOKEN_CACHE = "schemaCache";
  public static final String UNAUTHORIZED_MESSAGE = "401 Unauthorized";
  public static final String INSTANCE_ID = "instanceId";
  public static final String EVENTS = "events";
  public static final String ENDPOINT = "endpoint";
  public static final String SIGNING_KEY = "signingkey";
  public static final String RESOURCE = "resource";
  public static final String CREDENTIAL = "credentials";
  public static final String CREDENTIAL_TYPE_KEY = "type";
  public static final String CREDENTIAL_TYPE_TITLE = "title";
  public static final String CREDENTIAL_TYPE_REQUIRED = "required";
  public static final String SUBSCRIPTION_ID = "subscriptionId";
  public static final String EXTENSION_LIMIT = "extensionLimit";
  public static final String EXTENSIONS = "extensions";
  public static final String ACCESS_ENTITLEMENTS = "entitlements.access";
  public static final String ACCESS_ENTITLEMENTS_ALLOW = "entitlements.access.allow";
  public static final String ACCESS_ENTITLEMENTS_DENY = "entitlements.access.deny";
  public static final String DATA_ENTITLEMENTS = "entitlements.data";
  public static final String DATA_ENTITLEMENTS_ORIGIN = "entitlements.data.origin";
  public static final String DATA_ENTITLEMENTS_TRANSACTIONS = "entitlements.data.transactions";
  public static final int MAX_EXTENSION_LIMIT = 100;
  public static final String ENTITLEMENTS = "entitlements";
  public static final String DATA_ATTRIBUTE = "data";
  public static final String ALLOW_ATTRIBUTE = "allow";
  public static final String DENY_ATTRIBUTE = "deny";
  public static final String ACCESS_ATTRIBUTE = "access";
  public static final String REQUEST_TYPES_ATTRIBUTE = "requestTypes";
  public static final String TAG_ATTRIBUTE = "tags";
  public static final String LISTING_NAME = "listingName";
  public static final String LISTING_NAME_COLUMN = "listing_name";
  public static final String CATEGORIES = "categories";
  public static final String APPLICATIONS = "applications";
  public static final String WORKFLOWS = "workflows";

  public static final String ACCESS_FILTER_NAME = "accessFilter";
  public static final String ACCESS_FILTER_PARAM_TENANT_ID = "tenantId";
  public static final String EMPTY_REGEX = "^$";
  public static final String FIELDS = "fields";
  public static final String FORMATS = "formats";
  public static final String RESOURCES = "resources";
  public static final String CONDITIONS = "conditions";
  public static final String EXPORTS = "exports";
  public static final String DOC_TYPE = "docType";
  public static final String CONDITIONS_TYPE = "conditions.type";
  public static final String CONDITIONS_REQUIRED = "conditions.required";
  public static final String RESOURCE_TYPE = "resourceType";
  public static final String DEFAULT_SCHEMA_VERSION = "billing_rule_schema-2.0.0.json";
  public static final String TRANSFORMATTIONS = "transformations";
  public static final String TRANSACTIONS = DATA_ENTITLEMENTS + ".transactions";
  public static final String SCHEMA = "schema";
  public static final String SCHEMA_URI_KEY = "$" + SCHEMA;
  public static final String SCHEMA_URI_DRAFT_07 = "http://json-schema.org/draft-07/schema#";
  public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
  public static final String OPTIONS_META_SCHEMA = SCHEMA + "/options_meta_schema.json";
  public static final String TYPE_KEY = "type";
  public static final String TYPE_VALUE = "object";
  public static final String PROPERTIES_KEY = "properties";
  public static final String CLOSING_SQUARE_BRACKET = "]";
  public static final String TRANSACTIONS_ATTRIBUTE = "transactions";
  public static final String INTERFACE_URL = "interfaceUrl";
  public static final String ADMIN_INTERFACE_URL = "adminInterfaceUrl";

  // doc_types
  public static final String MI_DOC_TYPE = "urn:elli:document:type:ilad-mismo-mi-v3.4";
  public static final String LOAN_DELIVERY_FANNIE_DOC_TYPE =
      "urn:elli:document:type:loandelivery-fannie";
  public static final String FANNIE_DOC_TYPE = "urn:elli:document:type:fannie32";
  public static final String FREDDIE_DOC_TYPE = "urn:elli:document:type:loandelivery-freddie";
  public static final String CLOSING_EXTENDED_DOC_TYPE =
      "urn:elli:document:type:closing26-extended";
  public static final String UCD_DOC_TYPE = "urn:elli:document:type:ucd";
  public static final String UCD_FINAL_DOC_TYPE = "urn:elli:document:type:ucd-final";
  public static final String CD33_DOC_TYPE = "urn:elli:document:type:cd33";
  public static final String FREDDIE42_DOC_TYPE = "urn:elli:document:type:FREDDIE42";
  public static final String AUS24_DOC_TYPE = "urn:elli:document:type:aus24";
  public static final String CLOSING231_LOCK_FORM_DOC_TYPE =
      "urn:elli:document:type:closing231-lockform";
  public static final String LE33_DOC_TYPE = "urn:elli:document:type:le33";
  public static final String ULADDU_DOC_TYPE = "urn:elli:document:type:uladdu";
  public static final String ULADPA_DOC_TYPE = "urn:elli:document:type:uladpa";
  public static final String ILAD_DOC_TYPE = "urn:elli:document:type:ilad";

  public static final String ASTERISK = "*";
  public static final String EPC2 = "EPC2";
  public static final String CODE = "code";
  public static final String NAME = "name";
  public static final String SENDERS = "senders";
  public static final String FEATURE = "feature";
  public static final String ADDITIONAL_LINKS = "additionalLinks";
}
