package com.synkrato.services.partner.util;

import static com.synkrato.services.epc.common.EpcCommonConstants.CLIENT_ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.ENVIRONMENT;
import static com.synkrato.services.epc.common.EpcCommonConstants.EPC_BIZ_DEV_ROLE;
import static com.synkrato.services.epc.common.EpcCommonConstants.EPC_INTERNAL_ADMIN_ROLE;
import static com.synkrato.services.epc.common.EpcCommonConstants.ID_ATTRIBUTE;
import static com.synkrato.services.epc.common.EpcCommonConstants.INTEGRATION_TYPE;
import static com.synkrato.services.epc.common.EpcCommonConstants.NAME;
import static com.synkrato.services.epc.common.EpcCommonConstants.NAME_ATTRIBUTE;
import static com.synkrato.services.epc.common.EpcCommonConstants.PARTNER_ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.STATUS_ATTRIBUTE;
import static com.synkrato.services.epc.common.EpcCommonConstants.WEBHOOK;
import static com.synkrato.services.partner.PartnerServiceConstants.ACCESS_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.ALLOW_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.APPLICATIONS;
import static com.synkrato.services.partner.PartnerServiceConstants.CATEGORIES;
import static com.synkrato.services.partner.PartnerServiceConstants.CREDENTIAL;
import static com.synkrato.services.partner.PartnerServiceConstants.DATA_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.DENY_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.ENDPOINT;
import static com.synkrato.services.partner.PartnerServiceConstants.ENTITLEMENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.EVENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.EXTENSION_LIMIT;
import static com.synkrato.services.partner.PartnerServiceConstants.INSTANCE_ID;
import static com.synkrato.services.partner.PartnerServiceConstants.LISTING_NAME;
import static com.synkrato.services.partner.PartnerServiceConstants.REQUEST_TYPES_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.RESOURCE;
import static com.synkrato.services.partner.PartnerServiceConstants.SIGNING_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.TAG_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.WORKFLOWS;
import static com.synkrato.services.partner.util.TestUtil.getRootPath;

import com.synkrato.components.microservice.identity.IdentityContext;
import com.synkrato.components.microservice.identity.IdentityData;
import com.synkrato.services.epc.common.dto.AccessEntitlementDTO;
import com.synkrato.services.epc.common.dto.AdditionalLinkDTO;
import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.ConditionDTO;
import com.synkrato.services.epc.common.dto.CredentialDTO;
import com.synkrato.services.epc.common.dto.EntitlementDTO;
import com.synkrato.services.epc.common.dto.ExportDTO;
import com.synkrato.services.epc.common.dto.FeatureDTO;
import com.synkrato.services.epc.common.dto.FieldDTO;
import com.synkrato.services.epc.common.dto.FindingDTO;
import com.synkrato.services.epc.common.dto.FindingTypeDTO;
import com.synkrato.services.epc.common.dto.ManifestDTO;
import com.synkrato.services.epc.common.dto.OptionsDTO;
import com.synkrato.services.epc.common.dto.PlmAttributeDef;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.ResultDTO;
import com.synkrato.services.epc.common.dto.ServiceEventDTO;
import com.synkrato.services.epc.common.dto.ServiceEventTypeDTO;
import com.synkrato.services.epc.common.dto.TransactionEntitlementDTO;
import com.synkrato.services.epc.common.dto.TransformationDTO;
import com.synkrato.services.epc.common.dto.WebhookDTO;
import com.synkrato.services.epc.common.dto.enums.AccessScope;
import com.synkrato.services.epc.common.dto.enums.AdditionalLinkType;
import com.synkrato.services.epc.common.dto.enums.BillingRuleStatus;
import com.synkrato.services.epc.common.dto.enums.EnvironmentType;
import com.synkrato.services.epc.common.dto.enums.IntegrationType;
import com.synkrato.services.epc.common.dto.enums.ProductStatusType;
import com.synkrato.services.epc.common.dto.enums.PropertyType;
import com.synkrato.services.epc.common.dto.enums.TagType;
import com.synkrato.services.epc.common.kafka.ProductKafkaEvent.Payload;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.partner.data.AccessEntitlement;
import com.synkrato.services.partner.data.BillingRule;
import com.synkrato.services.partner.data.BillingRuleChangeLog;
import com.synkrato.services.partner.data.Export;
import com.synkrato.services.partner.data.Field;
import com.synkrato.services.partner.data.Manifest;
import com.synkrato.services.partner.data.ManifestRequest;
import com.synkrato.services.partner.data.Options;
import com.synkrato.services.partner.data.Product;
import com.synkrato.services.partner.data.TransactionEntitlement;
import com.synkrato.services.partner.data.Transformation;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;

public class TestHelper {

  public static final String TEST_PARTNER_ID = "007001";
  public static final String TEST_PRODUCT_NAME = "ProductandPricingOptimalBlue";
  public static final String TEST_PARTNER_URL =
      "https://api.optimalblue.qa.epc2.rd.synkrato.io/v1/productPrices";
  public static final String TEST_LISTING_NAME = "OptimalBlueProductandPricing";
  public static final String TEST_INTERFACE_URL = "http://optimalblue.qa.epc2.rd.synkrato.io";
  public static final String TEST_ADMIN_INTERFACE_URL =
      "http://testAdminInterfaceURl.qa.epc2.rd.synkrato.io";
  public static final String TEST_CREATED_USER = "urn:elli:service:soo";
  public static final String TEST_TAG_KEY = "categories";
  public static final String TEST_TAG_VALUE = "vod";
  public static final String WEBHOOK_EVENTS = "events";
  public static final String WEBHOOK_RESOURCE = "resource";
  public static final String WEBHOOK_SIGNING_KEY = "signingkey";
  public static final String WEBHOOK_SIGNING_KEY_VALUE = "test123";
  public static final String WEBHOOK_URL = "url";
  public static final String PARTNER_URL = "partnerUrl";
  public static final String INTERFACE_URL = "interfaceUrl";
  public static final String ADMIN_INTERFACE_URL = "adminInterfaceUrl";
  public static final String ID = CommonUtil.getUniqueIdAsString();

  public static Map<String, Object> buildAsyncProductMap() {

    Map<String, Object> productRequest = new HashMap<>();
    productRequest.put(ID_ATTRIBUTE, ID);
    productRequest.put(PARTNER_URL, TEST_PARTNER_URL);
    productRequest.put(INTERFACE_URL, TEST_INTERFACE_URL);
    productRequest.put(ADMIN_INTERFACE_URL, TEST_ADMIN_INTERFACE_URL);
    productRequest.put(ENVIRONMENT, EnvironmentType.sandbox);
    productRequest.put(STATUS_ATTRIBUTE, ProductStatusType.development);
    productRequest.put(PARTNER_ID, TEST_PARTNER_ID);
    productRequest.put(NAME, TEST_PRODUCT_NAME);
    productRequest.put(INTEGRATION_TYPE, IntegrationType.ASYNC.getDescription());
    productRequest.put(LISTING_NAME, TEST_LISTING_NAME);
    productRequest.put(REQUEST_TYPES_ATTRIBUTE, Arrays.asList("SEARCH", "REFRESH"));
    productRequest.put(TAG_ATTRIBUTE, buildTags());
    productRequest.put(CREDENTIAL, buildCredential());
    productRequest.put(ENTITLEMENTS, buildEntitlements());
    productRequest.put(WEBHOOK, buildWebhook());
    productRequest.put(EXTENSION_LIMIT, 0);

    return productRequest;
  }

  public static List buildWebhook() {

    Map<String, Object> transactionWebhook = new HashMap<>();
    transactionWebhook.put(WEBHOOK_URL, "www.test.com");
    transactionWebhook.put(WEBHOOK_RESOURCE, "urn:elli:epc:transaction");
    List<String> transactionEvents = new ArrayList<>();
    transactionEvents.add("created");
    transactionEvents.add("updated");
    transactionWebhook.put(WEBHOOK_EVENTS, transactionEvents);
    transactionWebhook.put(WEBHOOK_SIGNING_KEY, WEBHOOK_SIGNING_KEY_VALUE);

    Map<String, Object> transactionEventWebhook = new HashMap<>();
    transactionEventWebhook.put(WEBHOOK_URL, "www.test.com");
    transactionEventWebhook.put(WEBHOOK_RESOURCE, "urn:elli:epc:transaction:events");
    List<String> transactionNotificationEvents = new ArrayList<>();
    transactionNotificationEvents.add("created");
    transactionEventWebhook.put(WEBHOOK_EVENTS, transactionNotificationEvents);
    transactionEventWebhook.put(WEBHOOK_SIGNING_KEY, WEBHOOK_SIGNING_KEY_VALUE);

    List webhook = new ArrayList();
    webhook.add(transactionEventWebhook);
    webhook.add(transactionWebhook);

    return webhook;
  }

  public static List buildCredential() {

    List<Map<String, Object>> credential = new ArrayList<>();

    Map<String, Object> accountIdProperty = new HashMap<>();
    accountIdProperty.put("id", "accountId");
    accountIdProperty.put("type", "string");
    accountIdProperty.put("title", "Account Id");
    accountIdProperty.put("minimum", 4);
    accountIdProperty.put("maximum", 30);
    accountIdProperty.put("scope", "company");
    accountIdProperty.put("required", "true");

    Map<String, Object> usernameProperty = new HashMap<>();
    usernameProperty.put("id", "username");
    usernameProperty.put("type", "string");
    usernameProperty.put("title", "Username");
    usernameProperty.put("minimum", 8);
    usernameProperty.put("maximum", 15);
    usernameProperty.put("scope", "user");
    usernameProperty.put("required", "true");

    credential.add(accountIdProperty);
    credential.add(usernameProperty);
    return credential;
  }

  /**
   * @return Build and return tags
   */
  public static Map<String, List<String>> buildTags() {

    Map<String, List<String>> tags = new HashMap<>();
    tags.put(CATEGORIES, Arrays.asList("CREDIT", "VERIF", "credit"));
    tags.put(WORKFLOWS, Arrays.asList("INTERACTIVE", "AUTOMATED"));
    tags.put(APPLICATIONS, Arrays.asList("LO Connect", "Consumer Connect"));
    return tags;
  }

  /**
   * Build Access and Data Entitlements
   *
   * @return
   */
  public static Map<String, Object> buildEntitlements() {
    Map<String, Object> entitlements = new HashMap<>();
    entitlements.put(ACCESS_ATTRIBUTE, buildAccessEntitlements());

    try {
      entitlements.put(DATA_ATTRIBUTE, TestUtil.convertToMap(getRootPath() + "manifest.json"));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return entitlements;
  }

  /**
   * Build Access Entitlements
   *
   * @return
   */
  public static Map<String, Object> buildAccessEntitlements() {

    Map<String, Object> accessEntitlements = new HashMap<>();

    List<String> allow = new ArrayList<>();
    allow.add("BE11176282");

    List<String> deny = new ArrayList<>();
    deny.add("BE11176283");

    accessEntitlements.put(ALLOW_ATTRIBUTE, allow);
    accessEntitlements.put(DENY_ATTRIBUTE, deny);

    return accessEntitlements;
  }

  /**
   * This method will return default billingRuleDTO
   *
   * @return new billingRuleDTO that mimics user request
   */
  public static BillingRuleDTO buildBillingRuleDTO() {
    BillingRuleDTO billingRuleDTO = BillingRuleDTO.builder().build();

    billingRuleDTO.setId(UUID.randomUUID().toString());
    billingRuleDTO.setStatus(BillingRuleStatus.DRAFT);

    List<TransformationDTO> transformations = new ArrayList<>();
    Map<String, Object> transaction = new HashMap<>();
    transaction.put("requestType", "SEARCH");
    transaction.put("status", "completed");
    transaction.put("scope", "borrower");

    List<Map<String, Object>> groupingRules = new ArrayList<>();

    Map<String, Object> groupingRule = new HashMap<>();
    groupingRule.put("groupAs", "SKU-200");
    groupingRule.put("period", 30);
    groupingRule.put("ignore", false);

    groupingRules.add(groupingRule);

    TransformationDTO transformationDTO =
        TransformationDTO.builder()
            .sku("SKU-1001")
            .transaction(transaction)
            .groupingRules(groupingRules)
            .build();

    transformations.add(transformationDTO);

    billingRuleDTO.setTransformations(transformations);

    return billingRuleDTO;
  }

  public static List<BillingRuleDTO> buildBillingRulesDTOList() {
    List<BillingRuleDTO> billingRules = new ArrayList<>();

    billingRules.add(buildBillingRuleDTO());
    billingRules.add(buildBillingRuleDTO());
    billingRules.add(buildBillingRuleDTO());

    billingRules.get(0).setId(UUID.randomUUID().toString());
    billingRules.get(1).setId(UUID.randomUUID().toString());
    billingRules.get(2).setId(UUID.randomUUID().toString());

    billingRules.get(0).setStatus(BillingRuleStatus.DRAFT);
    billingRules.get(1).setStatus(BillingRuleStatus.DRAFT);
    billingRules.get(2).setStatus(BillingRuleStatus.DRAFT);

    return billingRules;
  }

  /**
   * This method will return the billingRule schema
   *
   * @return returns billingRuleSchema
   */
  public static String getBillingRuleSchema() {
    return "{\n"
        + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n"
        + "  \"type\": \"object\",\n"
        + "  \"description\": \"Transform definitions that qualify certain transaction states into billable SKU's, evaluated in order\",\n"
        + "  \"properties\": {\n"
        + "    \"transformations\": {\n"
        + "      \"type\": \"array\",\n"
        + "      \"items\": {\n"
        + "        \"$ref\": \"#/definitions/transformItem\"\n"
        + "      },\n"
        + "      \"minItems\": 1,\n"
        + "      \"description\": \"Transform definitions that qualify certain transaction states into billable SKU's, evaluated in order\"\n"
        + "    }\n"
        + "  },\n"
        + "  \"additionalProperties\": false,\n"
        + "  \"definitions\": {\n"
        + "    \"integerField\": {\n"
        + "      \"type\": \"integer\",\n"
        + "      \"minimum\": 1,\n"
        + "      \"maximum\": 100\n"
        + "    },\n"
        + "    \"numericField\": {\n"
        + "      \"type\": \"number\"\n"
        + "    },\n"
        + "    \"datePattern\": {\n"
        + "      \"type\": \"string\",\n"
        + "      \"pattern\": \"^^(?:(?:(?:0?[13578]|1[02])(\\\\/)31)\\\\1|(?:(?:0?[1,3-9]|1[0-2])(\\\\/)(?:29|30)\\\\2))(?:(?:1[6-9]|[2-9]\\\\d)?\\\\d{2})$|^(?:0?2(\\\\/)29\\\\3(?:(?:(?:1[6-9]|[2-9]\\\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:(?:0?[1-9])|(?:1[0-2]))(\\\\/)(?:0?[1-9]|1\\\\d|2[0-8])\\\\4(?:(?:1[6-9]|[2-9]\\\\d)?\\\\d{2})$\",\n"
        + "      \"validationMessage\": \"Date should be in the format MM/DD/YYYY.\"\n"
        + "    },\n"
        + "    \"scopePattern\": {\n"
        + "      \"type\": \"string\",\n"
        + "      \"pattern\": \"^(?i)(Loan|Borrower|Coborrower)$\",\n"
        + "      \"validationMessage\": \"(Scope can only be Loan or Borrower or Coborrower)\"\n"
        + "    },\n"
        + "    \"transformItem\": {\n"
        + "      \"type\": \"object\",\n"
        + "      \"properties\": {\n"
        + "        \"sku\": {\n"
        + "          \"type\": \"string\"\n"
        + "        },\n"
        + "        \"transaction\": {\n"
        + "          \"type\": \"object\",\n"
        + "          \"properties\": {\n"
        + "            \"requestType\": {\n"
        + "              \"type\": \"string\",\n"
        + "              \"description\": \"Qualifying transaction request type\"\n"
        + "            },\n"
        + "            \"status\": {\n"
        + "              \"type\": \"string\",\n"
        + "              \"description\": \"Qualifying transaction status\"\n"
        + "            },\n"
        + "            \"scope\": {\n"
        + "              \"$ref\": \"#/definitions/scopePattern\",\n"
        + "              \"description\": \"Qualifying transaction scope\"\n"
        + "            }\n"
        + "          },\n"
        + "          \"required\": [\n"
        + "            \"requestType\"\n"
        + "          ],\n"
        + "          \"additionalProperties\": false\n"
        + "        },\n"
        + "        \"groupingRules\": {\n"
        + "          \"type\": \"array\",\n"
        + "          \"description\": \"Grouping rules that qualify related transactions to be recorded with an alternative SKU, evaluated in order\",\n"
        + "          \"items\": [\n"
        + "            {\n"
        + "              \"type\": \"object\",\n"
        + "              \"properties\": {\n"
        + "                \"groupAs\": {\n"
        + "                  \"type\": \"string\",\n"
        + "                  \"description\": \"Target SKU\"\n"
        + "                },\n"
        + "                \"period\": {\n"
        + "                  \"$ref\": \"#/definitions/integerField\",\n"
        + "                  \"description\": \"Optional condition for qualifying number of days\"\n"
        + "                },\n"
        + "                \"ignore\": {\n"
        + "                  \"type\": \"boolean\",\n"
        + "                  \"description\": \"Optional condition to ignore billing on the transaction\"\n"
        + "                }\n"
        + "              },\n"
        + "              \"required\": [\n"
        + "                \"groupAs\",\n"
        + "                \"period\"\n"
        + "              ],\n"
        + "              \"additionalProperties\": false\n"
        + "            }\n"
        + "          ]\n"
        + "        }\n"
        + "      },\n"
        + "      \"required\": [\n"
        + "        \"sku\",\n"
        + "        \"transaction\"\n"
        + "      ],\n"
        + "      \"additionalProperties\": false\n"
        + "    }\n"
        + "  }\n"
        + "}";
  }

  /**
   * This method will return default billingRuleDTO
   *
   * @return new billingRuleDTO that mimics user request
   */
  public static BillingRule buildBillingRule() {
    BillingRule billingRule = BillingRule.builder().build();

    List<Transformation> transformations = new ArrayList<>();
    Map<String, Object> transaction = new HashMap<>();
    transaction.put("requestType", "Asset Verification");
    transaction.put("status", "completed");
    transaction.put("scope", "borrower");

    Transformation transformation =
        Transformation.builder().sku("SKU-1001").transaction(transaction).build();

    transformations.add(transformation);

    billingRule.setTransformations(transformations);
    billingRule.setSchemaVersion("billing_rule_schema-1.0.0.json");
    billingRule.setStatus(BillingRuleStatus.DRAFT.getValue());

    return billingRule;
  }

  public static List<BillingRule> buildBillingRulesList() {
    List<BillingRule> billingRules = new ArrayList<>();

    billingRules.add(buildBillingRule());
    billingRules.add(buildBillingRule());
    billingRules.add(buildBillingRule());

    billingRules.get(0).setId(UUID.randomUUID());
    billingRules.get(1).setId(UUID.randomUUID());
    billingRules.get(2).setId(UUID.randomUUID());

    billingRules.get(0).setStatus(BillingRuleStatus.DRAFT.toString());
    billingRules.get(1).setStatus(BillingRuleStatus.DRAFT.toString());
    billingRules.get(2).setStatus(BillingRuleStatus.DRAFT.toString());

    return billingRules;
  }

  /**
   * This method will create a billing rule change log
   *
   * @return
   */
  public static BillingRuleChangeLog buildBillingRuleChangeLog() {
    BillingRuleChangeLog changeLog =
        BillingRuleChangeLog.builder()
            .changesetId(0)
            .billingRuleId(UUID.randomUUID())
            .content(
                new ObjectMapper()
                    .convertValue(
                        buildBillingRuleDTO(), new TypeReference<Map<String, Object>>() {}))
            .operation("CREATED")
            .comments("Billing rule created")
            .build();

    return changeLog;
  }

  public static ProductDTO buildProductDTO() {

    List<String> requestTypeList = new ArrayList<>();
    requestTypeList.add("SEARCH");
    requestTypeList.add("NEWREQUEST");

    Map<TagType, List<String>> stringListMap = new HashMap<>();
    List<String> tagList = new ArrayList<>();
    tagList.add("credit");
    tagList.add("vod");
    tagList.add("appraisal");
    tagList.add("APPRAISAL");
    stringListMap.put(TagType.CATEGORIES, tagList);

    return ProductDTO.builder()
        .id(ID)
        .partnerId(TEST_PARTNER_ID)
        .name(TEST_PRODUCT_NAME)
        .partnerUrl(TEST_PARTNER_URL)
        .listingName(TEST_LISTING_NAME)
        .integrationType(IntegrationType.ASYNC)
        .interfaceUrl(TEST_INTERFACE_URL)
        .adminInterfaceUrl(TEST_ADMIN_INTERFACE_URL)
        .additionalLinks(buildAdditionalLinksDTO())
        .environment(EnvironmentType.sandbox)
        .status(ProductStatusType.development)
        .requestTypes(requestTypeList)
        .tags(stringListMap)
        .entitlements(buildEntitlementDTO(ID))
        .webhooks(buildWebhookDTO())
        .credentials(buildCredentialDTO())
        .feature(buildFeatureDTO())
        .extensionLimit(0)
        .build();
  }

  private static List<AdditionalLinkDTO> buildAdditionalLinksDTO() {
    List<AdditionalLinkDTO> additionalLinkDTOList = new ArrayList<>();
    additionalLinkDTOList.add(
        AdditionalLinkDTO.builder()
            .type(AdditionalLinkType.PRODUCT_CONFIG_URL)
            .url("url")
            .description("description")
            .altText("altText")
            .build());
    return additionalLinkDTOList;
  }

  public static FeatureDTO buildFeatureDTO() {
    return FeatureDTO.builder()
        .receiveAutomatedTransactionUpdates(true)
        .receiveResourceTypes(true)
        .sendResourceTypes(true)
        .build();
  }

  public static EntitlementDTO buildEntitlementDTO(String productId) {

    List<String> allowList =
        Arrays.asList(
            "urn:elli:encompass:instance:BE11176282:.*",
            "urn:elli:encompass:instance:BE1119999:.*");

    List<String> denyList = Arrays.asList("urn:elli:encompass:instance:BE999999:.*");

    return EntitlementDTO.builder()
        .access(AccessEntitlementDTO.builder().allow(allowList).deny(denyList).build())
        .data(buildManifestDTO(productId))
        .build();
  }

  public static List<Options> buildOptionDTO() {

    List<String> requestTypes = Arrays.asList("New Order");
    List<Options> optionsList =
        Arrays.asList(Options.builder().requestTypes(requestTypes).schema(new HashMap<>()).build());
    return optionsList;
  }

  public static ManifestDTO buildManifestDTO(String productId) {

    List<FieldDTO> fieldDTOList = new ArrayList<>();

    FieldDTO fieldDTO = new FieldDTO();
    fieldDTO.setFieldId("MORNET.X67");
    fieldDTO.setDescription("Fannie Mae Loan Doc Type Code");
    fieldDTO.setJsonPath("$.loanProductData.loanDocumentationType");
    fieldDTOList.add(fieldDTO);

    FieldDTO fieldDTO1 = new FieldDTO();
    fieldDTO1.setFieldId("MORNET.X67");
    fieldDTO1.setDescription("Fannie Mae Loan Doc Type Code");
    fieldDTO1.setJsonPath("$.loanProductData.loanDocumentationType");
    fieldDTOList.add(fieldDTO1);

    FieldDTO fieldDTO2 = new FieldDTO();
    fieldDTO2.setFieldId("FR0128");
    fieldDTO2.setDescription("Borr Present Country Code");
    fieldDTO2.setJsonPath(
        "$.applications[0].borrower.residences[?(@.residencyType == 'Current')][0].addressStreetLine1");
    fieldDTOList.add(fieldDTO2);

    PlmAttributeDef plmAttributeDef = new PlmAttributeDef();
    plmAttributeDef.setFields(fieldDTOList);

    return ManifestDTO.builder()
        .createdBy(TEST_CREATED_USER)
        .updatedBy(TEST_CREATED_USER)
        .created(CommonUtil.getCurrentISODateAsString())
        .updated(CommonUtil.getCurrentISODateAsString())
        .origin(plmAttributeDef)
        .transactions(buildTransactionEntitlementDTOList("SEARCH"))
        .build();
  }

  public static List<WebhookDTO> buildWebhookDTO() {

    List<String> events = new ArrayList<>();
    events.add("created");
    events.add("updated");

    WebhookDTO webhookDTO =
        WebhookDTO.builder()
            .subscriptionId("4f0cea55-4b6a-412f-bb76-25219a8afb2b")
            .url(
                "https://webhook.site/#!/717d1256-9bdf-4af2-9eb8-9cf072a90fba/efb5213b-d620-4659-a4bd-6fb7acd3861b/1")
            .signingkey("BQ1dOoYdb52Lkjsrhei7PjAf3*gGARdg8S3@fz2UAA1gRn^bKcRU")
            .resource("urn:elli:epc:transaction")
            .events(events)
            .build();

    List<WebhookDTO> webhookDTOList = new ArrayList<>();
    webhookDTOList.add(webhookDTO);

    return webhookDTOList;
  }

  public static List<OptionsDTO> buildOptionsDTOList() throws IOException {
    List<OptionsDTO> optionsDTOs = new ArrayList<>();
    OptionsDTO optionsDTO = new OptionsDTO();
    optionsDTO.setRequestTypes(Arrays.asList("NEWREQUEST"));
    optionsDTO.setSchema(getFileContent("options_schema.json"));
    optionsDTOs.add(optionsDTO);

    optionsDTO = new OptionsDTO();
    optionsDTO.setRequestTypes(Arrays.asList("SEARCH"));
    optionsDTO.setSchema(getFileContent("options_schema.json"));
    optionsDTOs.add(optionsDTO);
    return optionsDTOs;
  }

  /**
   * Builds partner credential schema dto object from json payload
   *
   * @return credentialDto object
   */
  public static List<CredentialDTO> buildCredentialDTO() {

    List<CredentialDTO> credentialDTO = new ArrayList<>();

    CredentialDTO credentialDTOUser =
        CredentialDTO.builder()
            .id("username")
            .type(PropertyType.TYPE_STRING)
            .title("Username")
            .minimum(4)
            .maximum(30)
            .scope(AccessScope.company)
            .required(true)
            .build();

    CredentialDTO credentialDTOPassword =
        CredentialDTO.builder()
            .id("password")
            .type(PropertyType.TYPE_STRING)
            .title("Password")
            .pattern("^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$")
            .minimum(8)
            .maximum(30)
            .scope(AccessScope.user)
            .required(true)
            .build();

    CredentialDTO credentialDTObranchId =
        CredentialDTO.builder()
            .id("branchId")
            .type(PropertyType.TYPE_STRING)
            .title("BranchId")
            .minimum(4)
            .maximum(30)
            .scope(AccessScope.company)
            .required(true)
            .build();

    credentialDTO.add(credentialDTOUser);
    credentialDTO.add(credentialDTOPassword);
    credentialDTO.add(credentialDTObranchId);

    return credentialDTO;
  }

  public static Product buildProduct(ProductDTO productDTO) {

    Product product = new Product();
    BeanUtils.copyProperties(productDTO, product);
    product.setId(CommonUtil.getUniqueId());
    product.setCreatedBy(TEST_CREATED_USER);
    product.setUpdatedBy(TEST_CREATED_USER);
    product.setCreated(CommonUtil.getCurrentISODate());
    product.setUpdated(CommonUtil.getCurrentISODate());
    product.setPartnerId(productDTO.getPartnerId());
    product.setStatus(productDTO.getStatus().name());
    product.setEnvironment(productDTO.getEnvironment().name());
    product.setIntegrationType(productDTO.getIntegrationType().name());
    if (Objects.nonNull(productDTO.getEntitlements())) {
      product.setAccessEntitlements(buildAccessEntitlement());
      product.setManifest(buildManifest(productDTO.getEntitlements().getData()));
    }
    product.setCredentials(new ArrayList<>());
    BeanUtils.copyProperties(productDTO.getCredentials(), product.getCredentials());

    return product;
  }

  /**
   * Builds options dto list from options db data
   *
   * @param optionsList data list
   * @return OptionsDTO list
   */
  private static List<Options> buildOptions(List<OptionsDTO> optionsList) {

    List<Options> optionsDTOS = new ArrayList<>();

    for (OptionsDTO optionDTO : optionsList) {
      Options options = new Options();
      BeanUtils.copyProperties(optionDTO, options);
      optionsDTOS.add(options);
    }

    return optionsDTOS;
  }

  public static AccessEntitlement buildAccessEntitlement() {
    return AccessEntitlement.builder()
        .allow(
            "^urn:elli:encompass:instance:BE11176282:.*$|^urn:elli:encompass:instance:BE1119999:.*$")
        .deny("^urn:elli:encompass:instance:BE999999:.*$")
        .build();
  }

  private static Manifest buildManifest(ManifestDTO manifestDTO) {

    List<Field> fieldDTOList = new ArrayList<>();

    Field fieldDTO = new Field();
    fieldDTO.setFieldId("MORNET.X67");
    fieldDTO.setDescription("Fannie Mae Loan Doc Type Code");
    fieldDTO.setJsonPath("$.loanProductData.loanDocumentationType");
    fieldDTOList.add(fieldDTO);

    Field fieldDTO1 = new Field();
    fieldDTO1.setFieldId("MORNET.X67");
    fieldDTO1.setDescription("Fannie Mae Loan Doc Type Code");
    fieldDTO1.setJsonPath("$.loanProductData.loanDocumentationType");
    fieldDTOList.add(fieldDTO1);

    Field fieldDTO2 = new Field();
    fieldDTO2.setFieldId("FR0128");
    fieldDTO2.setDescription("Borr Present Country Code");
    fieldDTO2.setJsonPath(
        "$.applications[0].borrower.residences[?(@.residencyType == 'Current')][0].addressStreetLine1");
    fieldDTOList.add(fieldDTO2);

    ManifestRequest manifestRequest = ManifestRequest.builder().build();
    manifestRequest.setFields(fieldDTOList);

    Manifest manifest = new Manifest();

    manifest.setOrigin(manifestRequest);
    manifest.setTransactions(buildTransactionEntitlementList());
    manifest.setCreatedBy(TEST_CREATED_USER);
    manifest.setUpdatedBy(TEST_CREATED_USER);
    manifest.setCreated(CommonUtil.getCurrentISODate());
    manifest.setUpdated(CommonUtil.getCurrentISODate());

    return manifest;
  }

  public static List<WebhookDTO> buildSubscriptionList() {
    List<WebhookDTO> subscriptionList = new ArrayList<>();
    WebhookDTO webhookDTO1 =
        WebhookDTO.builder()
            .resource("urn:elli:epc:transaction:event")
            .events(Arrays.asList("create"))
            .url("www.testurl.com")
            .signingkey("BQ1dOoYdb52Lkjsrhei7PjAf3*gGARdg8S3@fz2UAA1gRn^bKcRU")
            .build();
    WebhookDTO webhookDTO2 =
        WebhookDTO.builder()
            .resource("urn:elli:epc:transaction")
            .events(Arrays.asList("update"))
            .url("www.testurl2.com")
            .signingkey("BQ1dOoYdb52Lkjsrhei7PjAf3*gGARdg8S3@fz2UAA1gRn^bKcRU")
            .build();
    subscriptionList.add(webhookDTO1);
    subscriptionList.add(webhookDTO2);
    return subscriptionList;
  }

  public static void buildPartnerJWT() {
    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_CLIENT_ID, TEST_PARTNER_ID);
    claims.put(IdentityData.CLAIM_INSTANCE_ID, "sandbox");
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Partner");
    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);
  }

  public static void buildLenderJWT() {
    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_CLIENT_ID, "300000010024");
    claims.put(IdentityData.CLAIM_INSTANCE_ID, "sandbox");
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Enterprise");
    claims.put(IdentityData.CLAIM_REALM, "urn:elli:encompass:instance:BE999999:site:foo");

    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);
  }

  public static void buildConsumerJWT() {
    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_REALM, "urn:elli:encompass:instance:BE999999:site:foo");
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Consumer");
    claims.put(IdentityData.CLAIM_SUBJECT, "test:sub");

    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);
  }

  public static void buildInvalidPartnerJWT() {
    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_CLIENT_ID, "300000010024");
    claims.put(IdentityData.CLAIM_INSTANCE_ID, "sandbox");
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Partner");
    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);
  }

  public static void buildInvalidEnvironmentPartnerJWT() {
    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_CLIENT_ID, TEST_PARTNER_ID);
    claims.put(IdentityData.CLAIM_INSTANCE_ID, "sandbox1");
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Partner");
    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);
  }

  public static void buildApplicationJWT() {
    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_CLIENT_ID, TEST_PARTNER_ID);
    claims.put(IdentityData.CLAIM_INSTANCE_ID, "sandbox");
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Application");
    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);
  }

  public static void buildInternalAdminJwt() {
    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_CLIENT_ID, TEST_PARTNER_ID);
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Application");
    List<String> roles = new ArrayList<>();
    roles.add(EPC_INTERNAL_ADMIN_ROLE);
    claims.put(IdentityData.CLAIM_ROLES, roles);
    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);
  }

  public static void buildBizDevUserJwt() {
    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_CLIENT_ID, TEST_PARTNER_ID);
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Application");
    List<String> roles = new ArrayList<>();
    roles.add(EPC_BIZ_DEV_ROLE);
    claims.put(IdentityData.CLAIM_ROLES, roles);
    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);
  }

  public static ProductSearchDTO buildSearchAttribute() {

    Map<String, List<String>> tagCriteria = new HashMap<>();
    List<String> tagValues = new ArrayList<>();
    tagValues.add("voa");
    tagValues.add("credit");
    tagCriteria.put(TagType.CATEGORIES.name(), tagValues);

    ProductSearchDTO searchAttribute =
        ProductSearchDTO.builder()
            .partnerId(TEST_PARTNER_ID)
            .name(TEST_PRODUCT_NAME)
            .tags(tagCriteria)
            .build();
    return searchAttribute;
  }

  public static Map<String, List<String>> buildQueryParams() {

    Map<String, List<String>> tagCriteria = new HashMap<>();
    List<String> tagValues = new ArrayList<>();
    tagValues.add("VOA");
    tagValues.add("CREDIT");
    tagValues.add("APPRAISAL");
    tagCriteria.put("categories", tagValues);

    return tagCriteria;
  }

  public static Map<String, Object> buildProductSearchMap() {

    Map<String, Object> productSearchMap = new HashMap<>();
    productSearchMap.put(NAME_ATTRIBUTE, "Test Product");
    productSearchMap.put("partnerId", TEST_PARTNER_ID);

    productSearchMap.put("categories", "voa,credit,APPRAISAL");
    productSearchMap.put("requestType", "Life of Loan");

    return productSearchMap;
  }

  public static boolean validateManifestValues(
      Map<String, String> mapJsonPathForValidation, Map<String, Object> appliedPLM) {

    boolean retValue = true;

    ObjectMapper mapper = new ObjectMapper();
    JsonNode sourceJsonNode = mapper.valueToTree(appliedPLM);
    DocumentContext loanDocumentContext = JsonPath.parse(sourceJsonNode.toString());

    for (String key : mapJsonPathForValidation.keySet()) {
      if (!mapJsonPathForValidation
          .get(key)
          .equalsIgnoreCase(loanDocumentContext.read(key).toString())) {
        retValue = false;
        break;
      }
    }

    return retValue;
  }

  public static Map<String, Object> getFullLoanObject(String loanContent) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(loanContent, new TypeReference<Map<String, Object>>() {});
  }

  public static Map<String, Object> getFileContent(String fileName) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(
        new ClassPathResource("options_schema.json").getFile(),
        new TypeReference<Map<String, Object>>() {});
  }

  public static ManifestDTO buildManifestDTOBasedOnManifest(String Manifest) throws Exception {

    ManifestDTO manifestDTO = new ManifestDTO();

    PlmAttributeDef plmAttributeDef =
        getPlmAttributeDef(Manifest, "$.entitlements.data.transactions[0].request.fields");

    List<String> requestTypes = new ArrayList<>();
    requestTypes.add("SEARCH");
    TransactionEntitlementDTO transactionEntitlementDTO =
        buildTransactionEntitlementDTO(requestTypes);
    if (Objects.nonNull(plmAttributeDef)) {
      transactionEntitlementDTO.setRequest(plmAttributeDef);
    }

    plmAttributeDef =
        getPlmAttributeDef(Manifest, "$.entitlements.data.transactions[0].response.fields");
    if (Objects.nonNull(plmAttributeDef)) {
      transactionEntitlementDTO.setResponse(plmAttributeDef);
    }

    List<TransactionEntitlementDTO> transactionEntitlementDTOList = new ArrayList<>();
    transactionEntitlementDTOList.add(transactionEntitlementDTO);
    manifestDTO.setTransactions(transactionEntitlementDTOList);

    plmAttributeDef = getPlmAttributeDef(Manifest, "$.entitlements.data.origin.fields");
    if (Objects.nonNull(plmAttributeDef)) {
      manifestDTO.setOrigin(plmAttributeDef);
    }

    return manifestDTO;
  }

  public static PlmAttributeDef getPlmAttributeDef(String Manifest, String JPath) {
    PlmAttributeDef plmAttributeDef = null;
    try {
      if (JsonPath.parse(Manifest).read(JPath) != null) {
        plmAttributeDef = new PlmAttributeDef();

        plmAttributeDef.setFields(
            new ObjectMapper()
                .readValue(
                    JsonPath.parse(Manifest).read(JPath).toString(),
                    new TypeReference<List<FieldDTO>>() {}));
      }
    } catch (Exception ex) {
    }

    return plmAttributeDef;
  }

  public static Map<String, String> buildProductPricingRequestLoanFieldsForValidation() {

    Map<String, String> obLoanFieldWithValues = new HashMap<>();

    obLoanFieldWithValues.put("$.loanProductData.loanDocumentationType", "FullDocumentation");
    obLoanFieldWithValues.put("$.applications[0].bottomRatioPercent", "24.212");
    obLoanFieldWithValues.put("$.applications[0].borrower.pledgedAssets", "true");
    obLoanFieldWithValues.put("$.applications[0].borrower.declarationsJIndicator", "true");
    obLoanFieldWithValues.put("$.applications[0].borrower.declarationsKIndicator", "true");
    obLoanFieldWithValues.put("$.applications[0].borrower.minFicoScore", "760");
    obLoanFieldWithValues.put("$.applications[0].borrower.firstName", "Paul");
    obLoanFieldWithValues.put("$.applications[0].borrower.lastName", "Testco");
    obLoanFieldWithValues.put("$.applications[0].borrower.middleName", "TestMiddleName");
    obLoanFieldWithValues.put("$.applications[0].borrower.suffixToName", "TestSuffix");
    obLoanFieldWithValues.put("$.applications[0].borrower.mobilePhone", "123-123-1234");
    obLoanFieldWithValues.put("$.applications[0].borrower.emailAddressText", "test@test.com");
    obLoanFieldWithValues.put("$.applications[0].borrower.birthDate", "2008-03-12");
    obLoanFieldWithValues.put("$.vaLoanData.firstTimeUse", "true");
    obLoanFieldWithValues.put(
        "$.applications[0].borrower.totalMonthlyIncomeMinusNetRentalAmount", "1111");
    obLoanFieldWithValues.put("$.tsum.numberOfMonthsReserves", "5");
    obLoanFieldWithValues.put("$.applications[0].borrower.pass30Days", "30");
    obLoanFieldWithValues.put("$.applications[0].borrower.pass60Days", "60");
    obLoanFieldWithValues.put("$.applications[0].borrower.pass90Days", "90");
    obLoanFieldWithValues.put("$.applications[0].borrower.pass120Days", "120");
    obLoanFieldWithValues.put("$.firstTimeHomebuyersIndicator", "false");
    obLoanFieldWithValues.put("$.rateLock.impoundWavied", "true");
    obLoanFieldWithValues.put("$.applications[0].borrower.bankruptcyIndicator", "false");
    obLoanFieldWithValues.put("$.applications[0].borrower.dateOfBankruptcy", "2000-08-05");
    obLoanFieldWithValues.put("$.applications[0].borrower.openBankruptcy2", "false");
    obLoanFieldWithValues.put("$.applications[0].borrower.priorBankruptcy2", "false");
    obLoanFieldWithValues.put("$.applications[0].borrower.bankruptcyStatus", "Dismissed");
    obLoanFieldWithValues.put("$.vaLoanData.typeOfVeteran", "Regular Military");
    obLoanFieldWithValues.put("$.applications[0].coborrower.firstName", "Alice");
    obLoanFieldWithValues.put("$.applications[0].coborrower.lastName", "Testco");
    obLoanFieldWithValues.put("$.applications[0].coborrower.middleName", "CoborrTestMiddleName");
    obLoanFieldWithValues.put("$.applications[0].coborrower.suffixToName", "CoborrTestSuffix");
    obLoanFieldWithValues.put("$.applications[0].coborrower.minFicoScore", "750");
    obLoanFieldWithValues.put("$.applications[0].coborrower.birthDate", "2008-03-12");
    obLoanFieldWithValues.put("$.applications[0].coborrower.intentToOccupyIndicator", "true");
    obLoanFieldWithValues.put("$.baseLoanAmount", "212500");
    obLoanFieldWithValues.put("$.miAndFundingFeeFinancedAmount", "100000");
    obLoanFieldWithValues.put("$.property.loanPurposeType", "Purchase");
    obLoanFieldWithValues.put("$.property.condotelIndicator", "false");
    obLoanFieldWithValues.put("$.property.nonwarrantableProjectIndicator", "false");
    obLoanFieldWithValues.put("$.loanProductData.lienPriorityType", "FirstLien");
    obLoanFieldWithValues.put("$.applications[0].coborrower.homePhoneNumber", "555-555-5553");
    obLoanFieldWithValues.put(
        "$.applications[0].coborrower.emailAddressText", "pmathew2010@gmail.com");
    obLoanFieldWithValues.put("$.pmiIndicator", "false");
    obLoanFieldWithValues.put("$.uldd.refinanceCashOutAmount", "52000");
    obLoanFieldWithValues.put("$.uldd.projectDesignType", "testprojectDesignType");
    obLoanFieldWithValues.put("$.uldd.manufacturedHomeWidthType", "testmanufacturedHomeWidthType");
    obLoanFieldWithValues.put("$.options.desiredPrice", "599999");
    obLoanFieldWithValues.put("$.options.desiredRate", "5");
    obLoanFieldWithValues.put("$.regulationZ.interestOnlyIndicator", "false");
    obLoanFieldWithValues.put("$.propertyAppraisedValueAmount", "250000");
    obLoanFieldWithValues.put("$.applications[0].propertyUsageType", "PrimaryResidence");
    obLoanFieldWithValues.put("$.property.streetAddress", "2109 NewHome Street");
    obLoanFieldWithValues.put("$.property.city", "Columbus");
    obLoanFieldWithValues.put("$.property.county", "Franklin");
    obLoanFieldWithValues.put("$.property.state", "OH");
    obLoanFieldWithValues.put("$.property.postalCode", "43206");
    obLoanFieldWithValues.put("$.loanProductData.gsePropertyType", "ManufacturedHomeCondoPUDCoOp");
    obLoanFieldWithValues.put("$.purchasePriceAmount", "250000");
    obLoanFieldWithValues.put("$.property.numberOfStories", "4");
    obLoanFieldWithValues.put("$.property.financedNumberOfUnits", "2");
    obLoanFieldWithValues.put(
        "$.applications[0].borrower.loanForeclosureOrJudgementIndicator", "false");
    obLoanFieldWithValues.put("$.regulationZ.initialDisclosureProvidedDate", "2019-11-01");
    obLoanFieldWithValues.put(
        "$.property.gseRefinancePurposeType", "NoCashOutFHAStreamlinedRefinance");
    obLoanFieldWithValues.put("$.loanPurposeOfRefinanceType", "InterestRateReductionRefinanceLoan");
    obLoanFieldWithValues.put("$.miAndFundingFeeTotalAmount", "100000");
    obLoanFieldWithValues.put("$.rateLock.penaltyTerm", "1 Year");
    obLoanFieldWithValues.put("$.vaLoanData.fundingFeeExempt", "Y");
    obLoanFieldWithValues.put("$.loCompensation.whoPaidCompensation", "Lender Paid");
    obLoanFieldWithValues.put("$.leadSource", "test");
    obLoanFieldWithValues.put(
        "$.applications[0].borrower.residences[0].addressStreetLine1", "171 ORANGE AV borrnothing");
    obLoanFieldWithValues.put(
        "$.applications[0].borrower.residences[0].addressCity", "Saint Augustine");
    obLoanFieldWithValues.put("$.applications[0].borrower.residences[0].addressState", "FL");
    obLoanFieldWithValues.put(
        "$.applications[0].borrower.residences[0].addressPostalCode", "32084");
    obLoanFieldWithValues.put("$.applications[0].borrower.homePhoneNumber", "555-555-5551");
    obLoanFieldWithValues.put(
        "$.applications[0].borrower.employment[0].phoneNumber", "555-555-5554");
    obLoanFieldWithValues.put(
        "$.applications[0].borrower.employment[0].selfEmployedIndicator", "true");
    obLoanFieldWithValues.put(
        "$.applications[0].coborrower.residences[0].addressStreetLine1",
        "1122 Boogie Ave  CoborrNothing");
    obLoanFieldWithValues.put(
        "$.applications[0].coborrower.residences[0].addressCity", "Kansas City");
    obLoanFieldWithValues.put("$.applications[0].coborrower.residences[0].addressState", "MO");
    obLoanFieldWithValues.put(
        "$.applications[0].coborrower.residences[0].addressPostalCode", "64108");
    obLoanFieldWithValues.put(
        "$.applications[0].coborrower.employment[0].phoneNumber", "343-534-5345");
    obLoanFieldWithValues.put(
        "$.applications[0].atrqmBorrowers[0].underwritingRiskAssessType",
        "testunderwritingRiskAssessType");

    return obLoanFieldWithValues;
  }

  public static Map<String, String> buildAppraisalRIOriginLoanFieldsForValidation() {

    Map<String, String> appraisalRIOrogin = new HashMap<>();

    appraisalRIOrogin.put(
        "$.applications[0].borrower.fullNameWithSuffix", "Paul TestMiddleName Testco TestSuffix");
    appraisalRIOrogin.put("$.applications[0].borrower.homePhoneNumber", "555-555-5551");
    appraisalRIOrogin.put("$.applications[0].borrower.employment[0].phoneNumber", "555-555-5554");
    appraisalRIOrogin.put("$.applications[0].borrower.mobilePhone", "123-123-1234");
    appraisalRIOrogin.put("$.applications[0].borrower.emailAddressText", "test@test.com");

    appraisalRIOrogin.put("$.applications[0].propertyUsageType", "PrimaryResidence");
    appraisalRIOrogin.put(
        "$.applications[0].coborrower.fullNameWithSuffix",
        "Alice CoborrTestMiddleName Testco CoborrTestSuffix");
    appraisalRIOrogin.put("$.applications[0].coborrower.homePhoneNumber", "555-555-5553");
    appraisalRIOrogin.put("$.applications[0].coborrower.employment[0].phoneNumber", "343-534-5345");
    appraisalRIOrogin.put("$.applications[0].coborrower.mobilePhone", "345-345-4353");
    appraisalRIOrogin.put("$.applications[0].coborrower.emailAddressText", "pmathew2010@gmail.com");
    appraisalRIOrogin.put("$.property.streetAddress", "2109 NewHome Street");
    appraisalRIOrogin.put("$.property.city", "Columbus");
    appraisalRIOrogin.put("$.property.county", "Franklin");
    appraisalRIOrogin.put("$.property.state", "OH");
    appraisalRIOrogin.put("$.property.postalCode", "43206");
    appraisalRIOrogin.put("$.property.loanPurposeType", "Purchase");
    appraisalRIOrogin.put("$.property.propertyUsageType", "PrimaryResidence");
    appraisalRIOrogin.put("$.property.propertyRightsType", "test");
    appraisalRIOrogin.put("$.lenderCaseIdentifier", "1707EM69111");
    appraisalRIOrogin.put("$.agencyCaseIdentifier", "12345678");
    appraisalRIOrogin.put("$.loanProductData.gsePropertyType", "ManufacturedHomeCondoPUDCoOp");
    appraisalRIOrogin.put("$.mortgageType", "Conventional");
    appraisalRIOrogin.put("$.borrowerRequestedLoanAmount", "212500");
    appraisalRIOrogin.put("$.purchasePriceAmount", "250000");
    appraisalRIOrogin.put("$.id", "741e7072-73c7-4128-82b5-eeb0d9a89969");
    appraisalRIOrogin.put("$.loanNumber", "2123131901EM47111");
    appraisalRIOrogin.put("$.regulationZ.borrowerIntendToContinueDate", "2019-10-31");
    appraisalRIOrogin.put("$.regulationZ.initialTilDisclosureProvidedDate", "2019-11-01");
    appraisalRIOrogin.put("$.contacts[0].contactType", "BUYERS_AGENT");
    appraisalRIOrogin.put("$.contacts[0].name", "Ellie Mae - Encompass Platform");
    appraisalRIOrogin.put("$.contacts[0].contactName", "Test agent name123");
    appraisalRIOrogin.put("$.contacts[0].phone", "408-359-7891");
    appraisalRIOrogin.put("$.contacts[0].cell", "913-555-11133");
    appraisalRIOrogin.put("$.contacts[0].email", "John.Realtor@workemail.com");
    appraisalRIOrogin.put("$.contacts[1].name", "Test");
    appraisalRIOrogin.put("$.contacts[1].contactName", "Test agent name");
    appraisalRIOrogin.put("$.contacts[1].address", "1851, allisonway");
    appraisalRIOrogin.put("$.contacts[1].city", "San Jose");
    appraisalRIOrogin.put("$.contacts[1].postalCode", "95132");
    appraisalRIOrogin.put("$.contacts[1].phone", "408-359-7891");
    appraisalRIOrogin.put("$.contacts[1].state", "CA");
    appraisalRIOrogin.put("$.contacts[1].fax", "124-587-3698 7111");
    appraisalRIOrogin.put("$.contacts[2].contactType", "SELLER");
    appraisalRIOrogin.put("$.contacts[2].name", "TestSELLER");
    appraisalRIOrogin.put("$.contacts[2].phone", "408-359-7891");
    appraisalRIOrogin.put("$.contacts[2].email", "John.SELLER@workemail.com");
    appraisalRIOrogin.put("$.contacts[2].businessPhone", "124-587-3698");
    appraisalRIOrogin.put("$.contacts[2].cell", "913-555-5585");
    appraisalRIOrogin.put("$.contacts[3].contactType", "SELLER2");
    appraisalRIOrogin.put("$.contacts[3].name", "TestSELLER");
    appraisalRIOrogin.put("$.contacts[3].phone", "408-359-7891");
    appraisalRIOrogin.put("$.contacts[3].email", "John.SELLER@workemail.com");
    appraisalRIOrogin.put("$.contacts[3].businessPhone", "124-587-3698");
    appraisalRIOrogin.put("$.contacts[3].cell", "913-555-5585");
    appraisalRIOrogin.put("$.contacts[4].contactType", "SELLER3");
    appraisalRIOrogin.put("$.contacts[4].name", "TestSELLER");
    appraisalRIOrogin.put("$.contacts[4].phone", "408-359-7891");
    appraisalRIOrogin.put("$.contacts[4].email", "John.SELLER@workemail.com");
    appraisalRIOrogin.put("$.contacts[4].businessPhone", "124-587-3698");
    appraisalRIOrogin.put("$.contacts[4].cell", "913-555-5585");
    appraisalRIOrogin.put("$.contacts[5].contactType", "SELLER4");
    appraisalRIOrogin.put("$.contacts[5].name", "TestSELLER");
    appraisalRIOrogin.put("$.contacts[5].phone", "408-359-7891");
    appraisalRIOrogin.put("$.contacts[5].email", "John.SELLER@workemail.com");
    appraisalRIOrogin.put("$.contacts[5].businessPhone", "124-587-3698");
    appraisalRIOrogin.put("$.contacts[5].cell", "913-555-5585");
    appraisalRIOrogin.put("$.contacts[6].contactType", "BUILDER");
    appraisalRIOrogin.put("$.contacts[6].contactName", "Test agent name123");
    appraisalRIOrogin.put("$.contacts[6].phone", "408-359-7891");
    appraisalRIOrogin.put("$.contacts[6].email", "John.Realtor@workemail.com");
    appraisalRIOrogin.put("$.contacts[6].cell", "913-555-11133");
    appraisalRIOrogin.put("$.contacts[7].contactType", "SELLERS_AGENT");
    appraisalRIOrogin.put("$.contacts[7].name", "Ellie Mae - Encompass Platform");
    appraisalRIOrogin.put("$.contacts[7].contactName", "Test agent name123");
    appraisalRIOrogin.put("$.contacts[7].phone", "408-359-7891");
    appraisalRIOrogin.put("$.contacts[7].email", "John.Realtor@workemail.com");
    appraisalRIOrogin.put("$.contacts[7].cell", "913-555-11133");
    appraisalRIOrogin.put("$.additionalRequests.appraisalContactForEntry", "Borrower");
    appraisalRIOrogin.put("$.additionalRequests.appraisalVacant", "true");
    appraisalRIOrogin.put("$.additionalRequests.appraisalLockBox", "true");
    appraisalRIOrogin.put("$.additionalRequests.appraisalKeyPickUp", "true");
    appraisalRIOrogin.put("$.additionalRequests.appraisalContactName", "Paul Testco");
    appraisalRIOrogin.put("$.additionalRequests.appraisalContactHomePhone", "555-555-5551");
    appraisalRIOrogin.put("$.additionalRequests.appraisalContactWorkPhone", "555-555-5554");
    appraisalRIOrogin.put("$.additionalRequests.appraisalContactCellPhone", "123-123-1231");
    appraisalRIOrogin.put("$.additionalRequests.appraisalContactEmail", "test@test.com");
    appraisalRIOrogin.put(
        "$.additionalRequests.appraisalDescription1", "testcomments1- REQUEST.X26");
    appraisalRIOrogin.put(
        "$.additionalRequests.appraisalDescription2", "testcomments2- REQUEST.X27");
    appraisalRIOrogin.put(
        "$.additionalRequests.appraisalDescription3", "testcomments3- REQUEST.X28");
    appraisalRIOrogin.put("$.fannieMae.mornetPlusCaseFileId", "test");

    return appraisalRIOrogin;
  }

  public static Map<String, String> buildAppraisalRIRequestLoanFieldsForValidation() {

    Map<String, String> appraisalRIRequest = new HashMap<>();

    appraisalRIRequest.put("$.fannieMae.mornetPlusCaseFileId", "test");
    appraisalRIRequest.put("$.contacts[0].contactName", "Test agent name123");
    appraisalRIRequest.put("$.contacts[0].email", "John.Realtor@workemail.com");
    appraisalRIRequest.put("$.contacts[0].cell", "913-555-11133");
    appraisalRIRequest.put("$.contacts[0].personalLicenseNumber", "personalLicenseNumberTest");
    appraisalRIRequest.put("$.contacts[1].name", "Test");
    appraisalRIRequest.put("$.contacts[1].contactName", "Test agent name");
    appraisalRIRequest.put("$.contacts[1].address", "1851, allisonway");
    appraisalRIRequest.put("$.contacts[1].city", "San Jose");
    appraisalRIRequest.put("$.contacts[1].postalCode", "95132");
    appraisalRIRequest.put("$.contacts[1].phone", "408-359-7891");
    appraisalRIRequest.put("$.contacts[1].state", "CA");
    appraisalRIRequest.put("$.contacts[1].fax", "124-587-3698 7111");
    appraisalRIRequest.put(
        "$.applications[0].borrower.mailingAddress.addressStreetLine1",
        "171 ORANGE AV borrnothing");
    appraisalRIRequest.put(
        "$.applications[0].borrower.mailingAddress.addressCity", "Saint Augustine");
    appraisalRIRequest.put("$.applications[0].borrower.mailingAddress.addressState", "FL");
    appraisalRIRequest.put("$.applications[0].borrower.mailingAddress.addressPostalCode", "32084");
    appraisalRIRequest.put(
        "$.loanProductData.borrowerEstimatedClosingDate", "2019-03-12T22:53:00Z");
    appraisalRIRequest.put("$.regulationZ.borrowerIntendToContinueDate", "2019-10-31");
    appraisalRIRequest.put("$.regulationZ.initialTilDisclosureProvidedDate", "2019-11-01");
    appraisalRIRequest.put("$.applications[0].borrower.mobilePhone", "123-123-1234");
    appraisalRIRequest.put("$.applications[0].borrower.emailAddressText", "test@test.com");
    appraisalRIRequest.put(
        "$.applications[0].borrower.fullNameWithSuffix", "Paul TestMiddleName Testco TestSuffix");
    appraisalRIRequest.put("$.applications[0].borrower.homePhoneNumber", "555-555-5551");
    appraisalRIRequest.put("$.applications[0].borrower.employment[0].phoneNumber", "555-555-5554");
    appraisalRIRequest.put(
        "$.applications[0].borrower.residences[0].addressStreetLine1", "171 ORANGE AV borrnothing");
    appraisalRIRequest.put(
        "$.applications[0].borrower.residences[0].addressCity", "Saint Augustine");
    appraisalRIRequest.put("$.applications[0].borrower.residences[0].addressState", "FL");
    appraisalRIRequest.put("$.applications[0].borrower.residences[0].addressPostalCode", "32084");
    appraisalRIRequest.put(
        "$.applications[0].coborrower.mailingAddress.addressStreetLine1",
        "1122 Boogie Ave  CoborrNothing");
    appraisalRIRequest.put(
        "$.applications[0].coborrower.mailingAddress.addressCity", "Kansas City");
    appraisalRIRequest.put("$.applications[0].coborrower.mailingAddress.addressState", "MO");
    appraisalRIRequest.put(
        "$.applications[0].coborrower.mailingAddress.addressPostalCode", "64108");
    appraisalRIRequest.put(
        "$.applications[0].coborrower.residences[0].addressStreetLine1",
        "1122 Boogie Ave  CoborrNothing");
    appraisalRIRequest.put("$.applications[0].coborrower.residences[0].addressCity", "Kansas City");
    appraisalRIRequest.put("$.applications[0].coborrower.residences[0].addressState", "MO");
    appraisalRIRequest.put("$.applications[0].coborrower.residences[0].addressPostalCode", "64108");
    appraisalRIRequest.put(
        "$.applications[0].coborrower.fullNameWithSuffix",
        "Alice CoborrTestMiddleName Testco CoborrTestSuffix");
    appraisalRIRequest.put("$.applications[0].coborrower.homePhoneNumber", "555-555-5553");
    appraisalRIRequest.put(
        "$.applications[0].coborrower.employment[0].phoneNumber", "343-534-5345");
    appraisalRIRequest.put("$.applications[0].coborrower.mobilePhone", "345-345-4353");
    appraisalRIRequest.put(
        "$.applications[0].coborrower.emailAddressText", "pmathew2010@gmail.com");
    appraisalRIRequest.put("$.additionalRequests.appraisalContactCellPhone", "123-123-1231");
    appraisalRIRequest.put("$.additionalRequests.appraisalContactEmail", "test@test.com");
    appraisalRIRequest.put("$.additionalRequests.appraisalContactForEntry", "Borrower");
    appraisalRIRequest.put("$.additionalRequests.appraisalContactName", "Paul Testco");
    appraisalRIRequest.put("$.additionalRequests.appraisalContactHomePhone", "555-555-5551");
    appraisalRIRequest.put("$.additionalRequests.appraisalContactWorkPhone", "555-555-5554");
    appraisalRIRequest.put("$.additionalRequests.appraisalDateOrdered", "2008-03-12");
    appraisalRIRequest.put("$.additionalRequests.appraisalVacant", "true");
    appraisalRIRequest.put("$.additionalRequests.appraisalLockBox", "true");
    appraisalRIRequest.put("$.additionalRequests.appraisalKeyPickUp", "true");
    appraisalRIRequest.put(
        "$.additionalRequests.appraisalDescription1", "testcomments1- REQUEST.X26");
    appraisalRIRequest.put(
        "$.additionalRequests.appraisalDescription2", "testcomments2- REQUEST.X27");
    appraisalRIRequest.put(
        "$.additionalRequests.appraisalDescription3", "testcomments3- REQUEST.X28");
    appraisalRIRequest.put("$.agencyCaseIdentifier", "12345678");
    appraisalRIRequest.put("$.contacts[2].address", "4140 Dublin Blvd. # 300");
    appraisalRIRequest.put("$.contacts[2].city", "Dublin");
    appraisalRIRequest.put("$.contacts[2].name", "Ellie Mae - Encompass Platform");
    appraisalRIRequest.put("$.contacts[2].postalCode", "94568");
    appraisalRIRequest.put("$.contacts[2].state", "CA");
    appraisalRIRequest.put("$.borrowerRequestedLoanAmount", "212500");
    appraisalRIRequest.put("$.id", "741e7072-73c7-4128-82b5-eeb0d9a89969");
    appraisalRIRequest.put("$.loanNumber", "2123131901EM47111");
    appraisalRIRequest.put("$.property.structureBuiltYear", "2017");
    appraisalRIRequest.put("$.property.streetAddress", "2109 NewHome Street");
    appraisalRIRequest.put("$.property.city", "Columbus");
    appraisalRIRequest.put("$.property.county", "Franklin");
    appraisalRIRequest.put("$.property.state", "OH");
    appraisalRIRequest.put("$.property.postalCode", "43206");
    appraisalRIRequest.put("$.property.loanPurposeType", "Purchase");
    appraisalRIRequest.put("$.property.propertyRightsType", "test");
    appraisalRIRequest.put("$.property.legalDescriptionText1", "Subject Property Legal Desc1");
    appraisalRIRequest.put("$.lenderCaseIdentifier", "1707EM69111");
    appraisalRIRequest.put("$.mortgageType", "Conventional");
    appraisalRIRequest.put("$.vaLoanData.emailToBeNotifiedWhenUploaded", "true");
    appraisalRIRequest.put("$.applications[0].propertyUsageType", "PrimaryResidence");
    appraisalRIRequest.put("$.hmda.censusTrack", "Subject Property Census Tract");

    return appraisalRIRequest;
  }

  public static Map<String, String> buildAppraisalRIResponseLoanFieldsForValidation() {

    Map<String, String> appraisalRIResponse = new HashMap<>();

    appraisalRIResponse.put("$.uldd.appraisalIdentifier", "test");
    appraisalRIResponse.put("$.uldd.propertyValuationEffectiveDate", "2019-03-12T15:53:22Z");
    appraisalRIResponse.put("$.uldd.fanniePropertyFormType", "testfanniePropertyFormType");
    appraisalRIResponse.put("$.uldd.unit1TotalBedrooms", "testunit1TotalBedrooms");
    appraisalRIResponse.put("$.uldd.unit2TotalBedrooms", "unit2TotalBedrooms");
    appraisalRIResponse.put("$.uldd.unit3TotalBedrooms", "unit3TotalBedrooms");
    appraisalRIResponse.put("$.uldd.unit4TotalBedrooms", "unit4TotalBedrooms");
    appraisalRIResponse.put("$.uldd.condominiumProjectStatusType", "condominiumProjectStatusType");
    appraisalRIResponse.put("$.uldd.projectDesignType", "testprojectDesignType");
    appraisalRIResponse.put(
        "$.uldd.freddieProjectClassificationType", "freddieProjectClassificationType");
    appraisalRIResponse.put(
        "$.uldd.fannieProjectClassificationType", "fannieProjectClassificationType");
    appraisalRIResponse.put("$.uldd.projectAttachmentType", "projectAttachmentType");
    appraisalRIResponse.put("$.uldd.projectUnitCount", "projectUnitCount");
    appraisalRIResponse.put("$.uldd.NumberOfUnitsSold", "NumberOfUnitsSold");
    appraisalRIResponse.put("$.uldd.gSEProjectType", "gSEProjectType");
    appraisalRIResponse.put("$.contacts[0].referenceNumber", "referenceNumber");
    appraisalRIResponse.put("$.contacts[0].appraisalMade", "appraisalMade");
    appraisalRIResponse.put("$.contacts[0].name", "Test");
    appraisalRIResponse.put("$.contacts[0].address", "1851, allisonway");
    appraisalRIResponse.put("$.contacts[0].city", "San Jose");
    appraisalRIResponse.put("$.contacts[0].state", "CA");
    appraisalRIResponse.put("$.contacts[0].postalCode", "95132");
    appraisalRIResponse.put("$.contacts[0].license", "license");
    appraisalRIResponse.put("$.contacts[0].bizLicenseAuthStateCode", "bizLicenseAuthStateCode");
    appraisalRIResponse.put("$.contacts[0].contactName", "Test agent name");
    appraisalRIResponse.put("$.contacts[0].personalLicenseNumber", "personalLicenseNumberTest");
    appraisalRIResponse.put(
        "$.contacts[0].personalLicenseAuthStateCode", "personalLicenseAuthStateCode");
    appraisalRIResponse.put("$.contacts[0].phone", "408-359-7891");
    appraisalRIResponse.put("$.contacts[0].fax", "124-587-3698 7111");
    appraisalRIResponse.put("$.contacts[0].email", "John.Realtor@workemail.com");
    appraisalRIResponse.put("$.contacts[1].insuranceNoOfBedrooms", "5");
    appraisalRIResponse.put("$.propertyAppraisedValueAmount", "250000");
    appraisalRIResponse.put("$.fhaVaLoan.eem.appraisedValue", "Subject Property Census Tract");
    appraisalRIResponse.put("$.rateLock.propertyAppraisedValueAmount", "250000");
    appraisalRIResponse.put(
        "$.underwriterSummary.supervisoryAppraiserLicenseNumber",
        "supervisoryAppraiserLicenseNumber");
    appraisalRIResponse.put("$.underwriterSummary.appraisalType", "appraisalType");
    appraisalRIResponse.put(
        "$.underwriterSummary.appraisalCompletedDate", "appraisalCompletedDate");
    appraisalRIResponse.put("$.fees[0].borPaidAmount", "borPaidAmount");
    appraisalRIResponse.put("$.property.structureBuiltYear", "2017");
    appraisalRIResponse.put("$.property.numberOfStories", "4");
    appraisalRIResponse.put("$.property.buildingStatusType", "Existing");
    appraisalRIResponse.put("$.property.legalDescriptionText1", "Subject Property Legal Desc1");
    appraisalRIResponse.put("$.property.assessorsParcelIdentifier", "assessorsParcelIdentifier");
    appraisalRIResponse.put("$.property.propertyRightsType", "test");
    appraisalRIResponse.put("$.hmda.censusTrack", "Subject Property Census Tract");
    appraisalRIResponse.put("$.hmda.propertyType", "propertyType");
    appraisalRIResponse.put("$.tsum.projectName", "projectName");
    appraisalRIResponse.put("$.tsum.cpmProjectId", "cpmProjectId");

    return appraisalRIResponse;
  }

  public static Map<String, Object> buildSubscriptionPayload() {

    Map<String, Object> payload = new HashMap<>();

    payload.put(CLIENT_ID, "007001");
    payload.put(INSTANCE_ID, "TestProduct");
    List<String> events = new ArrayList<>();
    events.add("created");
    events.add("updated");
    payload.put(EVENTS, events);
    payload.put(SIGNING_KEY, "ABC1232333@1233");
    payload.put(ENDPOINT, "http://test.com/1");
    payload.put(RESOURCE, "urn:elli:epc:transaction");

    return payload;
  }

  public static List<Map<String, Object>> completePayload() {
    List<Map<String, Object>> finalPayload = new ArrayList<>();
    finalPayload.add(buildSubscriptionPayload());
    return finalPayload;
  }

  public static List<ConditionDTO> buildConditionsDTOList() {
    List<ConditionDTO> conditionDTOList = new ArrayList<>();

    List<String> requiredFields = new ArrayList<>();
    requiredFields.add("$.applications[0].borrower.firstName");
    requiredFields.add("$.applications[0].borrower.ssn");
    requiredFields.add("$.applications[0].borrower.lastName");

    ConditionDTO conditionDTO =
        ConditionDTO.builder().type("fields").required(requiredFields).build();

    conditionDTOList.add(conditionDTO);
    return conditionDTOList;
  }

  /** Build PlmAttributeDef Field list */
  public static List<FieldDTO> buildManifestFieldDTOList() {
    List<FieldDTO> fieldDTOList = new ArrayList<>();

    FieldDTO fieldDTO = FieldDTO.builder().build();
    fieldDTO.setFieldId("MORNET.X67");
    fieldDTO.setDescription("Fannie Mae Loan Doc Type Code");
    fieldDTO.setJsonPath("$.applications[0].borrower.firstName");
    fieldDTOList.add(fieldDTO);

    FieldDTO fieldDTO1 = FieldDTO.builder().build();
    fieldDTO1.setFieldId("MORNET.X67");
    fieldDTO1.setDescription("Fannie Mae Loan Doc Type Code");
    fieldDTO1.setJsonPath("$.applications[0].borrower.ssn");
    fieldDTOList.add(fieldDTO1);

    FieldDTO fieldDTO2 = FieldDTO.builder().build();
    fieldDTO2.setFieldId("FR0128");
    fieldDTO2.setDescription("Borr Present Country Code");
    fieldDTO2.setJsonPath("$.applications[0].borrower.lastName");
    fieldDTOList.add(fieldDTO2);

    return fieldDTOList;
  }

  /** Build Transaction Entitlement DTO */
  public static TransactionEntitlementDTO buildTransactionEntitlementDTO(
      List<String> requestTypes) {
    PlmAttributeDef requestManifest =
        PlmAttributeDef.builder()
            .fields(buildManifestFieldDTOList())
            .conditions(buildConditionsDTOList())
            .build();
    PlmAttributeDef responseManifest =
        PlmAttributeDef.builder().fields(buildManifestFieldDTOList()).build();

    return TransactionEntitlementDTO.builder()
        .requestTypes(requestTypes)
        .request(requestManifest)
        .response(responseManifest)
        .build();
  }

  /** Build TransactionEntitlementDTO List */
  public static List<TransactionEntitlementDTO> buildTransactionEntitlementDTOList(
      String... requestTypes) {
    List<TransactionEntitlementDTO> transactionEntitlementDTOList = new ArrayList<>();
    transactionEntitlementDTOList.add(buildTransactionEntitlementDTO(Arrays.asList(requestTypes)));
    return transactionEntitlementDTOList;
  }

  /** Build FindingtDTO List */
  public static FindingDTO buildFindingtDTO() {
    FindingTypeDTO findingTypeDTO = FindingTypeDTO.builder().build();
    findingTypeDTO.setCode("INC-101");
    findingTypeDTO.setName("Are there any employment gaps ≥ 30 days?");
    return FindingDTO.builder()
        .types(Collections.singletonList(findingTypeDTO))
        .statuses(Arrays.asList("open", "close"))
        .outboundStatuses(Arrays.asList("wait", "close"))
        .build();
  }

  /** Build ServiceEvents */
  public static ServiceEventDTO buildServiceEventDTO() {
    ServiceEventTypeDTO serviceEventTypeDTO = ServiceEventTypeDTO.builder().build();
    serviceEventTypeDTO.setCode("9010");
    serviceEventTypeDTO.setName("Request");
    serviceEventTypeDTO.setSenders(Arrays.asList("partner", "lender"));
    serviceEventTypeDTO.setType(Arrays.asList("automated", "manual"));
    return ServiceEventDTO.builder().types(Arrays.asList(serviceEventTypeDTO)).build();
  }

  /** Build Manifest Entity Fields */
  public static List<Field> buildManifestFields() {
    List<Field> fieldList = new ArrayList<>();

    Field field = new Field();
    field.setFieldId("MORNET.X67");
    field.setDescription("Fannie Mae Loan Doc Type Code");
    field.setJsonPath("$.loanProductData.loanDocumentationType");
    fieldList.add(field);

    Field field1 = new Field();
    field1.setFieldId("MORNET.X67");
    field1.setDescription("Fannie Mae Loan Doc Type Code");
    field1.setJsonPath("$.loanProductData.loanDocumentationType");
    fieldList.add(field1);

    Field field2 = new Field();
    field2.setFieldId("FR0128");
    field2.setDescription("Borr Present Country Code");
    field2.setJsonPath(
        "$.applications[0].borrower.residences[?(@.residencyType == 'Current')][0].addressStreetLine1");
    fieldList.add(field2);

    Field field3 = new Field();
    field3.setFieldId("MORNET.X67");
    field3.setDescription("Fannie Mae Loan Doc Type Code");
    field3.setJsonPath("$.loanProductData.loanDocumentationType[0][1]");
    fieldList.add(field3);

    return fieldList;
  }

  /** Build Transaction Entitlement Entity */
  public static TransactionEntitlement buildTransactionEntitlement(List<String> requestTrpes) {
    TransactionEntitlement transactionEntitlement = new TransactionEntitlement();
    transactionEntitlement.setRequestTypes(requestTrpes);
    ManifestRequest manifestRequest = ManifestRequest.builder().build();
    manifestRequest.setFields(buildManifestFields());
    transactionEntitlement.setRequest(manifestRequest);
    transactionEntitlement.setResponse(manifestRequest);
    return transactionEntitlement;
  }

  /** Build TransactionEntitlement entity List */
  public static List<TransactionEntitlement> buildTransactionEntitlementList(
      String... requestTypes) {
    List<TransactionEntitlement> transactionEntitlementList = new ArrayList<>();
    transactionEntitlementList.add(buildTransactionEntitlement(Arrays.asList(requestTypes)));
    return transactionEntitlementList;
  }

  public static List<FieldDTO> buildManifestVectorJsonPathFieldDTO() {
    List<FieldDTO> fieldList = new ArrayList<>();

    FieldDTO field1 = new FieldDTO();
    field1.setFieldId("4001");
    field1.setJsonPath("$.applications[0]");
    fieldList.add(field1);

    FieldDTO field3 = new FieldDTO();
    field3.setFieldId("FR0128");
    field3.setDescription("Borr Present residence Code");
    field3.setJsonPath(
        "$.applications[0].borrower.residences[(@.residencyType == 'Current')][0].addressStreetLine1");
    fieldList.add(field3);

    FieldDTO field2 = new FieldDTO();
    field2.setFieldId("FR0128");
    field2.setDescription("Borr Present residence Code");
    field2.setJsonPath("$.applications[0].borrower.residences[(@.residencyType == 'Current')]");
    fieldList.add(field2);

    return fieldList;
  }

  /** Build Manifest Entity Fields - Invalid test case */
  public static List<FieldDTO> buildManifestInvalidJsonPathFieldDTO() {
    List<FieldDTO> fieldList = new ArrayList<>();

    FieldDTO field = new FieldDTO();
    field.setFieldId("MORNET.X67");
    field.setDescription("Fannie Mae Loan Doc Type Code");
    field.setJsonPath("$loanProductData.loanDocumentationType");
    fieldList.add(field);

    FieldDTO field1 = new FieldDTO();
    field1.setFieldId("MORNET.X67");
    field1.setDescription("Fannie Mae Loan Doc Type Code");
    field1.setJsonPath("$.applications[0].borrower.firstName");
    fieldList.add(field1);

    FieldDTO field2 = new FieldDTO();
    field2.setFieldId("FR0128");
    field2.setDescription("Borr Present Country Code");
    field2.setJsonPath(
        "$.applications[0].borrower.residences[(@.residencyType == 'Current')][0].addressStreetLine1");
    fieldList.add(field2);

    FieldDTO field3 = new FieldDTO();
    field3.setFieldId("FR0128");
    field3.setDescription("Borr Present Country Code");
    field3.setJsonPath(
        "$.applications[0].borrower.residences[(@.residencyType == 'Current')][0].addressStreetLine1");
    fieldList.add(field3);

    FieldDTO field4 = new FieldDTO();
    field4.setFieldId("FR0128");
    field4.setDescription("Borr Present Country Code");
    field4.setJsonPath(
        "$.applications[0].borrower.residences[(.residencyType == 'Current')][0].addressStreetLine1");
    fieldList.add(field4);

    return fieldList;
  }

  public static List<ResultDTO> buildResults() {

    List<ResultDTO> listResultDTO = new ArrayList<>();

    ResultDTO resultDTOLock = buildResultDTO("LOCK");
    ResultDTO resultDTOReLock = buildResultDTO("RELOCK");
    listResultDTO.add(resultDTOLock);
    listResultDTO.add(resultDTOReLock);
    return listResultDTO;
  }

  public static ResultDTO buildResultDTO(String action) {

    List<String> listFormat = new ArrayList<>();
    listFormat.add("application/vnd.productpricing-lock-1.0.0+json");
    listFormat.add("application/vnd.productpricing-relock-1.0.0+json");
    listFormat.add("application/vnd.productpricing-extend-1.0.0+json");
    listFormat.add("application/vnd.productpricing-cancel-1.0.0+json");

    return ResultDTO.builder().action(action).formats(listFormat).build();
  }

  /**
   * build buildExportDTO
   *
   * @param docType docType
   * @return ExportDTO
   */
  public static ExportDTO buildExportDTO(String docType) {
    return ExportDTO.builder().docType(docType).overrideResources(true).build();
  }

  /**
   * build buildExportDTO
   *
   * @param docType docType
   * @return ExportDTO
   */
  public static Export buildExport(String docType) {
    return Export.builder().docType(docType).overrideResources(true).build();
  }

  /**
   * build kafka event payload
   *
   * @param product product
   * @return Payload
   */
  public static Payload buildKafkaProductPayload(Product product) {
    return Payload.builder()
        .partnerId(product.getPartnerId())
        .name(product.getName())
        .listingName(product.getListingName())
        .environment(product.getEnvironment())
        .status(product.getStatus())
        .extensionLimit(product.getExtensionLimit())
        .integrationType(product.getIntegrationType())
        .requestTypes(product.getRequestTypes())
        .tags(product.getTags())
        .build();
  }
}
