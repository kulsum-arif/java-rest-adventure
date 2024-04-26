package com.synkrato.services.partner.validators;

import static com.synkrato.services.epc.common.EpcCommonConstants.EMPTY_STRING;
import static com.synkrato.services.epc.common.EpcCommonConstants.ENVIRONMENT_ATTRIBUTE;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;
import static com.synkrato.services.epc.common.EpcCommonConstants.NOTIFICATION_EVENT_TYPE_CREATED;
import static com.synkrato.services.epc.common.EpcCommonConstants.NOTIFICATION_EVENT_TYPE_UPDATED;
import static com.synkrato.services.epc.common.EpcCommonConstants.NOTIFICATION_RESOURCE_LIST;
import static com.synkrato.services.epc.common.EpcCommonConstants.OPTIONS;
import static com.synkrato.services.epc.common.EpcCommonConstants.RESOURCES;
import static com.synkrato.services.epc.common.EpcCommonConstants.STATUS;
import static com.synkrato.services.epc.common.EpcCommonConstants.WEBHOOK;
import static com.synkrato.services.partner.PartnerServiceConstants.ACCESS_ENTITLEMENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.ADDITIONAL_LINKS;
import static com.synkrato.services.partner.PartnerServiceConstants.ASTERISK;
import static com.synkrato.services.partner.PartnerServiceConstants.AUS24_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.CD33_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.CLOSING231_LOCK_FORM_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.CLOSING_EXTENDED_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.CLOSING_SQUARE_BRACKET;
import static com.synkrato.services.partner.PartnerServiceConstants.CODE;
import static com.synkrato.services.partner.PartnerServiceConstants.CONDITIONS;
import static com.synkrato.services.partner.PartnerServiceConstants.CONDITIONS_REQUIRED;
import static com.synkrato.services.partner.PartnerServiceConstants.CONDITIONS_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.DATA_ENTITLEMENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.EXPORTS;
import static com.synkrato.services.partner.PartnerServiceConstants.EXTENSION_LIMIT;
import static com.synkrato.services.partner.PartnerServiceConstants.FANNIE_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.FEATURE;
import static com.synkrato.services.partner.PartnerServiceConstants.FIELDS;
import static com.synkrato.services.partner.PartnerServiceConstants.FORMATS;
import static com.synkrato.services.partner.PartnerServiceConstants.FREDDIE42_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.FREDDIE_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.ILAD_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.LE33_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.LOAN_DELIVERY_FANNIE_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.MAX_EXTENSION_LIMIT;
import static com.synkrato.services.partner.PartnerServiceConstants.MI_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.NAME;
import static com.synkrato.services.partner.PartnerServiceConstants.OPTIONS_META_SCHEMA;
import static com.synkrato.services.partner.PartnerServiceConstants.PROPERTIES_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.REQUEST_TYPES_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.RESOURCE_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.SCHEMA_URI_DRAFT_07;
import static com.synkrato.services.partner.PartnerServiceConstants.SCHEMA_URI_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.SENDERS;
import static com.synkrato.services.partner.PartnerServiceConstants.TAG_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.TYPE_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.TYPE_VALUE;
import static com.synkrato.services.partner.PartnerServiceConstants.UCD_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.UCD_FINAL_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.ULADDU_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.ULADPA_DOC_TYPE;
import static java.util.stream.Collectors.toList;

import com.amazonaws.services.s3.AmazonS3;
import com.synkrato.components.microservice.identity.IdentityContext;
import com.synkrato.services.epc.common.dto.AccessEntitlementDTO;
import com.synkrato.services.epc.common.dto.AdditionalLinkDTO;
import com.synkrato.services.epc.common.dto.CodeTypeDTO;
import com.synkrato.services.epc.common.dto.ConditionDTO;
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
import com.synkrato.services.epc.common.dto.ServiceEventTypeDTO;
import com.synkrato.services.epc.common.dto.TransactionEntitlementDTO;
import com.synkrato.services.epc.common.dto.WebhookDTO;
import com.synkrato.services.epc.common.dto.enums.AdditionalLinkType;
import com.synkrato.services.epc.common.dto.enums.EnvironmentType;
import com.synkrato.services.epc.common.dto.enums.IntegrationType;
import com.synkrato.services.epc.common.dto.enums.SenderType;
import com.synkrato.services.epc.common.dto.enums.ServiceEventType;
import com.synkrato.services.epc.common.dto.enums.TagType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.ProductService;
import com.synkrato.services.partner.cache.S3ClientCache;
import com.synkrato.services.partner.util.ProductUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.SchemaException;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Slf4j
@Component
public class ProductValidator implements Validator {

  static final String READ_ONLY_ATTRIBUTE_ERROR_MESSAGE =
      "synkrato.services.partner.product.readonly";
  static final Configuration JSON_PATH_CONFIGURATION =
      Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();
  static final DocumentContext jsonContext = JsonPath.using(JSON_PATH_CONFIGURATION).parse("{}");
  static final String EMPTY_ATTRIBUTE = "synkrato.services.partner.product.attributes.notEmpty";
  static final String INVALID_FINDING =
      "synkrato.services.partner.product.entitlements.findings.invalid.string";
  static final String DUPLICATE_ATTRIBUTE =
      "synkrato.services.partner.product.entitlements.duplicate";
  static final String EMPTY_ATTRIBUTE_WITH_INDEX =
      "synkrato.services.partner.product.entitlements.notEmpty.with-index";
  private static final String[] requiredSchemaAttributes = {
    SCHEMA_URI_KEY, TYPE_KEY, PROPERTIES_KEY
  };

  private static final Set<String> EXPORT_DOC_TYPE_FORMATS =
      Collections.unmodifiableSet(
          new HashSet<>(
              Arrays.asList(
                  MI_DOC_TYPE,
                  LOAN_DELIVERY_FANNIE_DOC_TYPE,
                  FANNIE_DOC_TYPE,
                  FREDDIE_DOC_TYPE,
                  CLOSING_EXTENDED_DOC_TYPE,
                  UCD_DOC_TYPE,
                  UCD_FINAL_DOC_TYPE,
                  CD33_DOC_TYPE,
                  FREDDIE42_DOC_TYPE,
                  AUS24_DOC_TYPE,
                  CLOSING231_LOCK_FORM_DOC_TYPE,
                  LE33_DOC_TYPE,
                  ULADDU_DOC_TYPE,
                  ULADPA_DOC_TYPE,
                  ILAD_DOC_TYPE)));
  private static final String FINDING_TYPE = "finding types";
  private static final String FINDING_STATUSES = "finding statuses";
  private static final String FINDING_OUTBOUND_STATUSES = "finding outbound statuses";
  private static final String SERVICE_EVENTS = "service events";
  private static final String SERVICE_EVENTS_TYPES = "service events types";
  @Autowired private MessageUtil messageUtil;
  @Autowired private ProductService productService;
  @Autowired private ProductUtil productUtil;
  @Autowired private S3ClientCache s3ClientCache;
  @Autowired private AmazonS3 amazonS3;
  @Autowired private ObjectMapper objectMapper;

  @Value("${synkrato.aws.s3.schema.name}")
  private String bucketName;

  @Value("${synkrato.aws.s3.schema.result-prefix}")
  private String prefix;

  /**
   * Predicate for stateful filter
   *
   * @param keyExtractor Predicate function
   * @param <T> Input Type to the function
   * @return Stateful filter prediate
   */
  private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Set<Object> uniqueResources = ConcurrentHashMap.newKeySet();
    return t -> uniqueResources.add(keyExtractor.apply(t));
  }

  @Override
  public boolean supports(Class<?> aClass) {
    return ProductDTO.class.equals(aClass);
  }

  @Override
  public void validate(Object product, Errors errors) {

    log.debug("product_validator {}", LOGGER_START);
    ProductDTO productDTO = (ProductDTO) product;
    validatePayload(productDTO);
    validatePartnerEnvironment(productDTO, errors);
    validateRequestTypes(productDTO, errors);
    validateDuplicateRequestType(productDTO, errors);
    validatePartnerUrl(productDTO, errors);
    validateWebhook(productDTO, errors);
    validateProductFeatures(productDTO.getFeature(), errors);
    validateEmptyWebhook(productDTO, errors);
    validateDataEntitlements(productDTO, errors);
    validateAccessEntitlements(productDTO, errors);
    validateExtensionLimit(productDTO, errors);
    validateOptions(productDTO, errors);
    validateAdditionalLinks(productDTO, errors);

    log.debug("product_validator {}", LOGGER_END);
  }

  /**
   * validates additional links
   *
   * @param productDTO
   * @param errors
   */
  private void validateAdditionalLinks(ProductDTO productDTO, Errors errors) {

    log.debug("validate_additional_links {}", LOGGER_START);

    if (Objects.isNull(productDTO.getAdditionalLinks())) {
      return;
    }

    if (productDTO.getAdditionalLinks().stream().anyMatch(Objects::isNull)) {
      throw new EpcRuntimeException(
          HttpStatus.BAD_REQUEST,
          messageUtil.getMessage(
              "synkrato.services.partner.product.invalid.content.error",
              new Object[] {ADDITIONAL_LINKS}));
    }
    List<AdditionalLinkType> additionalLinkTypeList =
        productDTO.getAdditionalLinks().stream()
            .filter(Objects::nonNull)
            .map(AdditionalLinkDTO::getType)
            .collect(Collectors.toList());

    if (additionalLinkTypeList.size() != additionalLinkTypeList.stream().distinct().count()) {
      errors.rejectValue(
          ADDITIONAL_LINKS, "synkrato.services.partner.product.additional-link.type.duplicate");
    }
    log.debug("validate_additional_links {}", LOGGER_END);
  }

  /**
   * validates incoming payload
   *
   * @param productDTO incoming product payload
   */
  private void validatePayload(ProductDTO productDTO) {
    if (Objects.nonNull(productDTO.getTags())) {
      validateTagsPayload(productDTO.getTags());
    }
  }

  /**
   * validates Tags payload and throws exception when dataType mismatch
   *
   * @param tagMap tags map
   */
  private void validateTagsPayload(Map<TagType, List<String>> tagMap) {
    for (Entry<TagType, List<String>> entry : tagMap.entrySet())
      if (Objects.nonNull(entry.getValue())
          && !productUtil.isValidDataType(entry.getValue(), ArrayList.class)) {
        throw new EpcRuntimeException(
            HttpStatus.BAD_REQUEST,
            messageUtil.getMessage(
                "synkrato.services.partner.product.invalid.content.error",
                new Object[] {TAG_ATTRIBUTE}));
      }
  }

  /**
   * @param methodParameter MethodParameter of the controller method
   * @param bindingResults BindingResults with error details
   * @throws MethodArgumentNotValidException when there are validation errors
   */
  public void handleErrors(MethodParameter methodParameter, BindingResult bindingResults)
      throws MethodArgumentNotValidException {
    throw new MethodArgumentNotValidException(methodParameter, bindingResults);
  }

  /**
   * Validate Partner environment
   *
   * @param productDTO Product details
   * @param errors Errors
   */
  private void validatePartnerEnvironment(ProductDTO productDTO, Errors errors) {

    boolean isParterEnvironmentValidFlag = false;
    String environment = CommonUtil.getProductEnvironment();

    if (!StringUtils.isEmpty(environment)) {
      isParterEnvironmentValidFlag =
          Stream.of(EnvironmentType.values())
              .anyMatch(environmentType -> environmentType.name().equalsIgnoreCase(environment));

      if (!isParterEnvironmentValidFlag) {
        errors.rejectValue(
            ENVIRONMENT_ATTRIBUTE,
            "synkrato.services.partner.product.environment.invalid",
            new Object[] {IdentityContext.get().getInstanceId()},
            EMPTY_STRING);
      }
    } else {
      errors.rejectValue(
          ENVIRONMENT_ATTRIBUTE, "synkrato.services.partner.product.environment.required");
    }
  }

  /**
   * Check Duplicate Request Type
   *
   * @param productDTO Product Details
   * @param errors Errors
   */
  private void validateDuplicateRequestType(ProductDTO productDTO, Errors errors) {
    if (!CollectionUtils.isEmpty(productDTO.getRequestTypes())) {
      long uniqueRequestTypeCount = productDTO.getRequestTypes().stream().distinct().count();
      if (productDTO.getRequestTypes().size() != uniqueRequestTypeCount) {
        errors.rejectValue(
            REQUEST_TYPES_ATTRIBUTE, "synkrato.services.partner.product.request-types.duplicate");
      }
    }
  }

  /**
   * Check Empty Request Type
   *
   * @param productDTO Product Details
   * @param errors Errors
   */
  private void validateRequestTypes(ProductDTO productDTO, Errors errors) {
    if (CollectionUtils.isEmpty(productDTO.getRequestTypes())) {
      errors.rejectValue(
          REQUEST_TYPES_ATTRIBUTE, "synkrato.services.partner.product.request-types.required");
    } else if (productDTO.getRequestTypes().contains(EMPTY_STRING)) {
      errors.rejectValue(
          REQUEST_TYPES_ATTRIBUTE, "synkrato.services.partner.product.request-types.empty");
    }
  }

  /**
   * Validate Partner Url for P2P Integration
   *
   * @param productDTO Product Details
   * @param errors Errors
   */
  private void validatePartnerUrl(ProductDTO productDTO, Errors errors) {
    if (IntegrationType.P2P.equals(productDTO.getIntegrationType())
        && StringUtils.isEmpty(productDTO.getPartnerUrl())) {
      errors.rejectValue("partnerUrl", "synkrato.services.partner.product.partner-url.required");
    }
  }

  /**
   * Validate Webhook attributes and check duplicate webhook subscriptions
   *
   * @param productDTO Product Details
   * @param errors Errors
   */
  private void validateWebhook(ProductDTO productDTO, Errors errors) {
    if (!CollectionUtils.isEmpty(productDTO.getWebhooks())) {
      validateWebhookAttributes(productDTO, errors);
      checkDuplicateWebhookSubscriptions(productDTO, errors);
    }
  }

  /**
   * Validate Webhook Attribute details
   *
   * @param productDTO Product Details
   * @param errors Errors
   */
  private void validateWebhookAttributes(ProductDTO productDTO, Errors errors) {
    for (WebhookDTO webhookDTO : productDTO.getWebhooks()) {
      if (StringUtils.isEmpty(webhookDTO.getUrl())) {
        errors.rejectValue(
            WEBHOOK, "synkrato.services.partner.product.webhook.partner-url.required");
      }
      if (CollectionUtils.isEmpty(webhookDTO.getEvents())) {
        errors.rejectValue(WEBHOOK, "synkrato.services.partner.product.webhook.event.required");
      } else {
        boolean isValidEventType =
            webhookDTO.getEvents().stream()
                .allMatch(
                    event ->
                        event.equals(NOTIFICATION_EVENT_TYPE_CREATED)
                            || event.equals(NOTIFICATION_EVENT_TYPE_UPDATED));
        if (!isValidEventType) {
          errors.rejectValue(WEBHOOK, "synkrato.services.partner.product.webhook.event.invalid");
        }
      }
      if (StringUtils.isEmpty(webhookDTO.getResource())) {
        errors.rejectValue(
            WEBHOOK, "synkrato.services.partner.product.webhook.resource-name.required");
      } else if (!NOTIFICATION_RESOURCE_LIST.contains(webhookDTO.getResource())) {
        errors.rejectValue(
            WEBHOOK, "synkrato.services.partner.product.webhook.resource-name.invalid");
      }
    }
  }

  /**
   * Check duplicate webhook subscriptions
   *
   * @param productDTO Product Details
   * @param errors Errors
   */
  private void checkDuplicateWebhookSubscriptions(ProductDTO productDTO, Errors errors) {

    if (!CollectionUtils.isEmpty(productDTO.getWebhooks())) {
      List<WebhookDTO> validWebhooks =
          productDTO.getWebhooks().stream()
              .filter(p -> !StringUtils.isEmpty(p.getResource()))
              .collect(toList());
      List<WebhookDTO> distinctWebhooks =
          validWebhooks.stream().filter(distinctByKey(WebhookDTO::getResource)).collect(toList());

      if (!CollectionUtils.isEmpty(distinctWebhooks)
          && validWebhooks.size() > distinctWebhooks.size()) {
        errors.rejectValue(WEBHOOK, "synkrato.services.product.webhook.resource-name.duplicate");
      }
    }
  }

  /**
   * @param productDTO input productDTO
   * @param errors errors
   */
  private void validateDataEntitlements(ProductDTO productDTO, Errors errors) {
    if (productUtil.hasDataEntitlements(productDTO.getEntitlements())) {

      // Validate Data entitlement for full schema only if not null
      // Admin need to pass origin,request,response to set data Entitlements for POST or PATCH APIs
      validateOriginDataEntitlements(productDTO.getEntitlements().getData(), errors);
      validateTransactionDataEntitlements(productDTO, errors);
      validateDataEntitlementJsonPaths(productDTO.getEntitlements().getData(), errors);
      validateManifestResults(productDTO.getEntitlements().getData(), errors);
      validateManifestResources(productDTO.getEntitlements().getData(), errors);
      validateFindings(productDTO, errors);
      validateServiceEvents(productDTO.getEntitlements().getData(), errors);
    }
  }

  /**
   * This method will validate Findings in product*
   *
   * @param productDTO
   * @param errors
   */
  private void validateFindings(ProductDTO productDTO, Errors errors) {

    if (Objects.isNull(productDTO)
        || Objects.isNull(productDTO.getEntitlements())
        || Objects.isNull(productDTO.getEntitlements().getData())) {
      return;
    }

    FindingDTO findingDTO = productDTO.getEntitlements().getData().getFindings();

    if (Objects.nonNull(findingDTO)) {
      validateFindingTypes(findingDTO.getTypes(), errors);
      validateStatuses(findingDTO.getStatuses(), FINDING_STATUSES, errors);
      validateStatuses(findingDTO.getOutboundStatuses(), FINDING_OUTBOUND_STATUSES, errors);
    }
  }

  /**
   * This method will validate ServiceEvents in product*
   *
   * @param manifestDTO
   * @param errors
   */
  private void validateServiceEvents(ManifestDTO manifestDTO, Errors errors) {

    if (Objects.isNull(manifestDTO.getServiceEvents())) {
      return;
    }

    List<ServiceEventTypeDTO> serviceEventTypes = manifestDTO.getServiceEvents().getTypes();

    if (CollectionUtils.isEmpty(serviceEventTypes)) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.attributes.notEmpty",
          new Object[] {serviceEventTypes == null ? SERVICE_EVENTS : SERVICE_EVENTS_TYPES},
          EMPTY_STRING);
      return;
    }

    Set<String> uniqueCodes = new HashSet<>();
    Set<String> duplicates = new HashSet<>();

    for (int i = 0; i < serviceEventTypes.stream().count(); i++) {

      ServiceEventTypeDTO serviceEventTypeDTO = serviceEventTypes.get(i);

      // checking code type
      validateCodeType(serviceEventTypeDTO, errors, i, uniqueCodes, duplicates);

      // checking if the senders is valid
      validateServiceEventSenders(serviceEventTypeDTO, errors, i);

      // checking if the types is valid
      validateServiceEventType(serviceEventTypeDTO, errors, i);
    }

    if (!duplicates.isEmpty()) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          DUPLICATE_ATTRIBUTE,
          new Object[] {CODE, String.join(", ", duplicates), SERVICE_EVENTS},
          EMPTY_STRING);
    }
  }

  /**
   * Validates type under serviceevents
   *
   * @param serviceEventTypeDTO
   * @param errors
   * @param serviceEventTypeIndex
   */
  private void validateServiceEventType(
      ServiceEventTypeDTO serviceEventTypeDTO, Errors errors, int serviceEventTypeIndex) {

    List<String> typeList = serviceEventTypeDTO.getType();
    if (ObjectUtils.isEmpty(typeList)) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          EMPTY_ATTRIBUTE_WITH_INDEX,
          new Object[] {TYPE_KEY, SERVICE_EVENTS, serviceEventTypeIndex},
          EMPTY_STRING);
      return;
    }

    // Check if valid enum value
    for (String type : typeList) {
      if (ObjectUtils.isEmpty(type) || ObjectUtils.isEmpty(type.trim())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            EMPTY_ATTRIBUTE_WITH_INDEX,
            new Object[] {TYPE_KEY, SERVICE_EVENTS, serviceEventTypeIndex},
            EMPTY_STRING);
      } else if (Objects.isNull(ServiceEventType.find(type))) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.service-events.type.invalid.with-index",
            new Object[] {type, serviceEventTypeIndex},
            EMPTY_STRING);
      }
    }

    // Check for duplicates
    if (typeList.size() != typeList.stream().distinct().count()) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.duplicate.with-index",
          new Object[] {SERVICE_EVENTS, TYPE_KEY, serviceEventTypeIndex},
          EMPTY_STRING);
    }
  }

  /**
   * Validate the codeTypeDTO
   *
   * @param codeTypeDTO
   * @param errors
   * @param index
   */
  private void validateCodeType(
      CodeTypeDTO codeTypeDTO,
      Errors errors,
      int index,
      Set<String> existingCodes,
      Set<String> duplicateCodes) {

    // fix the input before validations
    if (Objects.nonNull(codeTypeDTO.getCode())) {
      codeTypeDTO.setCode(codeTypeDTO.getCode().trim());
    }

    // check if code is blank
    if (ObjectUtils.isEmpty(codeTypeDTO.getCode())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          EMPTY_ATTRIBUTE_WITH_INDEX,
          new Object[] {CODE, SERVICE_EVENTS, index},
          EMPTY_STRING);
    }

    // checking if the name is blank
    if (ObjectUtils.isEmpty(codeTypeDTO.getName())
        || ObjectUtils.isEmpty(codeTypeDTO.getName().trim())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          EMPTY_ATTRIBUTE_WITH_INDEX,
          new Object[] {NAME, SERVICE_EVENTS, index},
          EMPTY_STRING);
    }

    if (!ObjectUtils.isEmpty(codeTypeDTO.getCode())
        && !existingCodes.add(codeTypeDTO.getCode().toLowerCase())) {
      duplicateCodes.add(codeTypeDTO.getCode());
    }
  }

  /**
   * Validates senders under serviceevents
   *
   * @param serviceEventTypeDTO
   * @param errors
   * @param serviceEventTypeIndex
   */
  private void validateServiceEventSenders(
      ServiceEventTypeDTO serviceEventTypeDTO, Errors errors, int serviceEventTypeIndex) {
    List<String> senders = serviceEventTypeDTO.getSenders();
    if (ObjectUtils.isEmpty(senders)) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          EMPTY_ATTRIBUTE_WITH_INDEX,
          new Object[] {SENDERS, SERVICE_EVENTS, serviceEventTypeIndex},
          EMPTY_STRING);
      return;
    }

    // Check if valid enum value
    for (String sender : senders) {
      if (ObjectUtils.isEmpty(sender) || ObjectUtils.isEmpty(sender.trim())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            EMPTY_ATTRIBUTE_WITH_INDEX,
            new Object[] {SENDERS, SERVICE_EVENTS, serviceEventTypeIndex},
            EMPTY_STRING);
      } else if (Objects.isNull(SenderType.find(sender))) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.service-events.sender.invalid.with-index",
            new Object[] {sender, serviceEventTypeIndex},
            EMPTY_STRING);
      }
    }

    // Check for duplicates
    if (senders.size() != senders.stream().distinct().count()) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.duplicate.with-index",
          new Object[] {SERVICE_EVENTS, SENDERS, serviceEventTypeIndex},
          EMPTY_STRING);
    }
  }

  /**
   * Validate data.transaction.request.exports field
   *
   * @param request
   * @param errors
   */
  private void validateExports(PlmAttributeDef request, Errors errors) {

    if (CollectionUtils.isEmpty(request.getExports())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.data.transactions.request.exports.notEmpty",
          new Object[] {EXPORTS},
          EMPTY_STRING);
    } else {

      Set<String> errorCodeAndValueSet = new HashSet<>();
      Set<String> docTypeSet = new HashSet<>();
      for (ExportDTO exportDTO : request.getExports()) {
        if (Objects.isNull(exportDTO) || ObjectUtils.isEmpty(exportDTO.getDocType())) {
          checkEmptyExports(errorCodeAndValueSet, errors);
        } else {
          checkUnSupportedDocType(exportDTO, errorCodeAndValueSet, errors);
          checkDuplicateDocType(exportDTO, errorCodeAndValueSet, docTypeSet, errors);
        }
      }
    }
  }

  /**
   * Check duplicate export docType
   *
   * @param exportDTO
   * @param errorCodeAndValueSet
   * @param docTypeSet
   * @param errors
   */
  private void checkDuplicateDocType(
      ExportDTO exportDTO,
      Set<String> errorCodeAndValueSet,
      Set<String> docTypeSet,
      Errors errors) {
    if (!docTypeSet.add(exportDTO.getDocType())
        && errorCodeAndValueSet.add(
            "synkrato.services.partner.product.entitlements.data.transactions.request.exports.doc-type.duplicate"
                + exportDTO.getDocType())) {

      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.data.transactions.request.exports.doc-type.duplicate",
          new Object[] {exportDTO.getDocType()},
          EMPTY_STRING);
    }
  }

  /**
   * Check un-supported export docType
   *
   * @param exportDTO
   * @param errorCodeAndValueSet
   * @param errors
   */
  private void checkUnSupportedDocType(
      ExportDTO exportDTO, Set<String> errorCodeAndValueSet, Errors errors) {
    if (!EXPORT_DOC_TYPE_FORMATS.contains(exportDTO.getDocType())
        && errorCodeAndValueSet.add(
            "synkrato.services.partner.product.entitlements.data.transactions.request.exports.doc-type.notSupported"
                + exportDTO.getDocType())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.data.transactions.request.exports.doc-type.notSupported",
          new Object[] {exportDTO.getDocType()},
          EMPTY_STRING);
    }
  }

  /**
   * throw errors when exports is empty
   *
   * @param errorCodeAndValueSet
   * @param errors
   */
  private void checkEmptyExports(Set<String> errorCodeAndValueSet, Errors errors) {
    if (errorCodeAndValueSet.add("synkrato.services.partner.product.attributes.notEmpty")) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.attributes.notEmpty",
          new Object[] {DOC_TYPE},
          EMPTY_STRING);
    }
  }

  /**
   * Validate Origin data entitlements
   *
   * @param manifestDTO input manifestDTO
   * @param errors errors
   */
  private void validateOriginDataEntitlements(ManifestDTO manifestDTO, Errors errors) {
    if (Objects.isNull(manifestDTO.getOrigin())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.data.required",
          new Object[] {"Origin"},
          EMPTY_STRING);

    } else {

      if (Objects.isNull(manifestDTO.getOrigin().getFields())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.data.entitlement.field.required",
            new Object[] {"Origin"},
            EMPTY_STRING);
      }

      if (Objects.nonNull(manifestDTO.getOrigin().getConditions())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.data.conditions.not-supported",
            new Object[] {"origin"},
            EMPTY_STRING);
      }
    }
  }

  /**
   * @param productDTO input manifestDTO
   * @param errors errors
   */
  private void validateTransactionDataEntitlements(ProductDTO productDTO, Errors errors) {
    if (Objects.isNull(productDTO.getEntitlements())
        || Objects.isNull(productDTO.getEntitlements().getData())
        || Objects.isNull(productDTO.getEntitlements().getData().getTransactions())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.data.required",
          new Object[] {"Request and response"},
          EMPTY_STRING);
    } else {
      validateAllTransactionDataEntitlements(productDTO, errors);
    }
  }

  /**
   * performs validation of options schema
   *
   * @param productDTO incoming product payload
   * @param errors captures if any error
   */
  private void validateOptions(ProductDTO productDTO, Errors errors) {
    log.debug("validate_options {}", LOGGER_START);

    if (!CollectionUtils.isEmpty(productDTO.getOptions())) {
      List<String> currentOptionsRequestTypes = new ArrayList<>();
      Set<String> errorCodes = new HashSet<>();

      productDTO
          .getOptions()
          .forEach(
              option -> {
                if (Objects.nonNull(option)) {

                  if (!CollectionUtils.isEmpty(option.getRequestTypes())) {
                    validateOptionsRequestTypes(
                        option, currentOptionsRequestTypes, productDTO, errorCodes);
                  }

                  if (Objects.nonNull(option.getSchema())) {
                    validateOptionsSchema(option, errors);
                  }
                }
              });

      errorCodes.forEach(errorCode -> errors.rejectValue(OPTIONS, errorCode));
    }

    log.debug("validate_options {}", LOGGER_END);
  }

  /**
   * Checks whether the options schema is valid
   *
   * @param options options payload
   */
  private void validateOptionsSchema(OptionsDTO options, Errors errors) {
    log.debug("is_valid_schema {}", LOGGER_START);

    try {
      if (isValidSchemaFieldsExist(options, errors)) {
        JSONObject metaSchema = new JSONObject(getOptionsMetaSchema());
        JSONObject optionsSchema = new JSONObject(options.getSchema());
        Schema schema = SchemaLoader.load(metaSchema);
        schema.validate(optionsSchema);
      }
    } catch (SchemaException | ValidationException schemaEx) {
      log.warn("Invalid options schema", schemaEx);
      errors.rejectValue(
          OPTIONS,
          "synkrato.services.partner.product.options.schema.validation.error",
          new Object[] {options.getRequestTypes()},
          EMPTY_STRING);
    }

    log.debug("is_valid_schema {}", LOGGER_END);
  }

  /**
   * Gets options meta schema from local
   *
   * @return meta schema as Map
   */
  private Map<String, Object> getOptionsMetaSchema() {
    try {
      ClassPathResource classPathResource = new ClassPathResource(OPTIONS_META_SCHEMA);
      String metaSchema =
          IOUtils.toString(classPathResource.getInputStream(), StandardCharsets.UTF_8);
      classPathResource.getInputStream().close();

      return objectMapper.readValue(metaSchema, new TypeReference<Map<String, Object>>() {});
    } catch (IOException ioException) {
      log.error("Unable to read options meta schema {]", ioException.getMessage(), ioException);
      throw new EpcRuntimeException("Unable to read options meta schema", ioException);
    }
  }

  /**
   * Checks whether the schema has valid attributes
   *
   * @param options schema content
   * @return true or false
   */
  private boolean isValidSchemaFieldsExist(OptionsDTO options, Errors errors) {
    log.debug("is_valid_schema_fields_exist {}", LOGGER_START);

    if (!isRequiredSchemaAttributesExist(options, errors)
        || isInvalidSchemaAttributesExist(options, errors)) {
      return false;
    }

    log.debug("is_valid_schema_fields_exist {}", LOGGER_END);
    return true;
  }

  /**
   * validates any invalid attributes exist in schema
   *
   * @param options schema content
   * @param errors list of errors
   * @return true or false
   */
  private boolean isInvalidSchemaAttributesExist(OptionsDTO options, Errors errors) {
    log.debug("is_invalid_schema_attributes_exist {}", LOGGER_START);

    boolean isInvalidSchemaAttributesExist = false;
    for (Map.Entry<String, Object> entry : options.getSchema().entrySet()) {
      switch (entry.getKey()) {
        case SCHEMA_URI_KEY:
          if (!entry.getValue().equals(SCHEMA_URI_DRAFT_07)) {
            isInvalidSchemaAttributesExist = true;
            addMissingSchemaAttributesError(SCHEMA_URI_KEY, SCHEMA_URI_DRAFT_07, options, errors);
          }
          break;
        case TYPE_KEY:
          if (!entry.getValue().equals(TYPE_VALUE)) {
            isInvalidSchemaAttributesExist = true;
            addMissingSchemaAttributesError(TYPE_KEY, TYPE_VALUE, options, errors);
          }
          break;
        default:
          break;
      }
    }

    if (options.getSchema().get(PROPERTIES_KEY) instanceof Map
        && ((Map) options.getSchema().get(PROPERTIES_KEY)).isEmpty()) {
      isInvalidSchemaAttributesExist = true;
      errors.rejectValue(
          OPTIONS,
          "synkrato.services.partner.product.options.schema.properties.missing",
          new Object[] {options.getRequestTypes()},
          EMPTY_STRING);
    } else if (!(options.getSchema().get(PROPERTIES_KEY) instanceof Map)) {
      isInvalidSchemaAttributesExist = true;
      errors.rejectValue(
          OPTIONS,
          "synkrato.services.partner.product.options.schema.properties.invalid",
          new Object[] {options.getRequestTypes()},
          EMPTY_STRING);
    }

    log.debug("is_invalid_schema_attributes_exist {}", LOGGER_END);
    return isInvalidSchemaAttributesExist;
  }

  /**
   * checks whether the required attributes exist in schema
   *
   * @param options schema content
   * @param errors list of errors
   * @return true or false
   */
  private boolean isRequiredSchemaAttributesExist(OptionsDTO options, Errors errors) {
    log.debug("is_required_schema_attributes_exist {}", LOGGER_START);

    boolean isRequiredSchemaAttributesExist = true;
    if (Arrays.stream(requiredSchemaAttributes)
        .anyMatch(
            item ->
                !options.getSchema().containsKey(item)
                    || Objects.isNull(options.getSchema().get(item)))) {
      isRequiredSchemaAttributesExist = false;
      errors.rejectValue(
          OPTIONS,
          "synkrato.services.partner.product.options.schema.attributes.missing",
          new Object[] {Arrays.asList(requiredSchemaAttributes), options.getRequestTypes()},
          EMPTY_STRING);
    }

    log.debug("is_required_schema_attributes_exist {}", LOGGER_END);
    return isRequiredSchemaAttributesExist;
  }

  /**
   * Validates requestTypes of each options schema
   *
   * @param option options schema
   * @param currentOptionsRequestTypes current request types of an options schema
   * @param productDTO product data
   * @param errorCodes stores list of error codes
   */
  private void validateOptionsRequestTypes(
      OptionsDTO option,
      List<String> currentOptionsRequestTypes,
      ProductDTO productDTO,
      Set<String> errorCodes) {

    log.debug("validate_options_schema_request_types {}", LOGGER_START);

    if (!productDTO.getRequestTypes().containsAll(option.getRequestTypes())) {
      errorCodes.add("synkrato.services.partner.product.options.requestTypes.not-found");
    } else {
      if (!Collections.disjoint(currentOptionsRequestTypes, option.getRequestTypes())) {
        errorCodes.add("synkrato.services.partner.product.options.requestTypes.duplicate");
      } else {
        currentOptionsRequestTypes.addAll(option.getRequestTypes());
      }

      if (option.getRequestTypes().stream().distinct().count() < option.getRequestTypes().size()) {
        errorCodes.add(
            "synkrato.services.partner.product.options.requestTypes.same-requestTypes.duplicate");
      }
    }

    log.debug("validate_options_schema_request_types {}", LOGGER_END);
  }

  /**
   * Validate Transaction data entitlements
   *
   * @param productDTO input manifestDTO
   * @param errors errors
   */
  private void validateAllTransactionDataEntitlements(ProductDTO productDTO, Errors errors) {
    log.debug("validate_all_transaction_data_entitlements {}", LOGGER_START);
    List<String> currentDataEntitlementRequestTypes = new ArrayList<>();
    Set<String> errorCodes = new HashSet<>();
    for (TransactionEntitlementDTO transactionEntitlementDTO :
        productDTO.getEntitlements().getData().getTransactions()) {

      if (CollectionUtils.isEmpty(transactionEntitlementDTO.getRequestTypes())) {
        errorCodes.add(
            "synkrato.services.partner.product.entitlements.data.transaction.request-type.required");
      } else {
        validateDataEntitlementRequestTypes(
            transactionEntitlementDTO,
            productDTO.getRequestTypes(),
            errorCodes,
            currentDataEntitlementRequestTypes);

        validateRequestDataEntitlements(transactionEntitlementDTO, errors);
        validateResponseDataEntitlements(transactionEntitlementDTO, errors);
      }
    }

    errorCodes.forEach(errorCode -> errors.rejectValue(DATA_ENTITLEMENTS, errorCode));
    log.debug("validate_all_transaction_data_entitlements {}", LOGGER_END);
  }

  /**
   * validates Data Entitlement RequestTypes
   *
   * @param transactionEntitlementDTO data entitlement item
   * @param requestTypes product request types
   * @param errorCodes list of error codes
   * @param currentDataEntitlementRequestTypes current data entitlement request types
   */
  private void validateDataEntitlementRequestTypes(
      TransactionEntitlementDTO transactionEntitlementDTO,
      List<String> requestTypes,
      Set<String> errorCodes,
      List<String> currentDataEntitlementRequestTypes) {
    log.debug("validate_data_entitlement_request_types {}", LOGGER_START);

    if (!CollectionUtils.isEmpty(requestTypes)
        && !requestTypes.containsAll(transactionEntitlementDTO.getRequestTypes())) {
      errorCodes.add("synkrato.services.partner.product.data-entitlements.requestTypes.not-found");
    } else {
      if (!Collections.disjoint(
          currentDataEntitlementRequestTypes, transactionEntitlementDTO.getRequestTypes())) {
        errorCodes.add(
            "synkrato.services.partner.product.data-entitlements.requestTypes.duplicate");
      } else {
        currentDataEntitlementRequestTypes.addAll(transactionEntitlementDTO.getRequestTypes());
      }

      if (transactionEntitlementDTO.getRequestTypes().stream().distinct().count()
          < transactionEntitlementDTO.getRequestTypes().size()) {
        errorCodes.add(
            "synkrato.services.partner.product.data-entitlements.requestTypes.same-requestTypes.duplicate");
      }
    }
    log.debug("validate_data_entitlement_request_types {}", LOGGER_END);
  }

  /**
   * Validate Request Data Entitlements
   *
   * @param transactionEntitlementDTO Input transactionEntitlementDTO
   * @param errors errors
   */
  private void validateRequestDataEntitlements(
      TransactionEntitlementDTO transactionEntitlementDTO, Errors errors) {
    if (Objects.isNull(transactionEntitlementDTO.getRequest())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.data.required",
          new Object[] {"Request"},
          EMPTY_STRING);
    } else {
      if (Objects.isNull(transactionEntitlementDTO.getRequest().getFields())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.data.entitlement.field.required",
            new Object[] {"Request"},
            EMPTY_STRING);
      }

      // Conditions are not mandatory - validation required only when conditions not empty
      if (Objects.nonNull(transactionEntitlementDTO.getRequest().getConditions())) {
        validateConditions(transactionEntitlementDTO.getRequest(), errors);
      }

      // Exports are not mandatory - validation required only when exports present.
      if (Objects.nonNull(transactionEntitlementDTO.getRequest().getExports())) {
        validateExports(transactionEntitlementDTO.getRequest(), errors);
      }
    }
  }

  /**
   * this method validates all partner required fields present in request manifest
   *
   * @param request manifest fields
   * @param errors list of validation errors
   */
  private void validateConditions(PlmAttributeDef request, Errors errors) {
    if (CollectionUtils.isEmpty(request.getConditions())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.attributes.notEmpty",
          new Object[] {CONDITIONS},
          EMPTY_STRING);
    } else {

      if (request.getConditions().stream()
              .filter(
                  condition ->
                      !StringUtils.isEmpty(condition.getType())
                          && condition.getType().equals(FIELDS))
              .count()
          > 1) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.data.conditions.type.duplicate.found");
      } else {
        for (ConditionDTO conditionDto : request.getConditions()) {
          validateType(conditionDto, errors);
          validateRequiredNode(request, conditionDto, errors);
        }
      }
    }
  }

  /**
   * adds missing schema attribute errors to list
   *
   * @param key missing key attribute
   * @param value missing value
   * @param errors list of errors
   */
  private void addMissingSchemaAttributesError(
      String key, String value, OptionsDTO options, Errors errors) {
    errors.rejectValue(
        OPTIONS,
        "synkrato.services.partner.product.options.schema.attributes.invalid",
        new Object[] {key, value, options.getRequestTypes()},
        EMPTY_STRING);
  }

  /**
   * is type is null or empty
   *
   * @param conditionDto condition object
   * @param errors list of errors
   */
  private void validateType(ConditionDTO conditionDto, Errors errors) {

    if (Objects.isNull(conditionDto.getType())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.attributes.required",
          new Object[] {CONDITIONS_TYPE},
          EMPTY_STRING);
    } else {
      if (StringUtils.isEmpty(conditionDto.getType().trim())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.attributes.notEmpty",
            new Object[] {CONDITIONS_TYPE},
            EMPTY_STRING);
      }
      // does type has valid value
      if (!conditionDto.getType().equals(FIELDS)) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.data.conditions.type.invalid");
      }
    }
  }

  /**
   * Checks any empty required fields
   *
   * @param conditionDto condition object
   * @param errors list of errors
   */
  private void validateRequiredNode(
      PlmAttributeDef request, ConditionDTO conditionDto, Errors errors) {
    if (Objects.isNull(conditionDto.getRequired())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.attributes.required",
          new Object[] {CONDITIONS_REQUIRED},
          EMPTY_STRING);
    } else {
      if (CollectionUtils.isEmpty(conditionDto.getRequired())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.attributes.notEmpty",
            new Object[] {CONDITIONS_REQUIRED},
            EMPTY_STRING);
      } else {

        if (conditionDto.getRequired().parallelStream()
            .anyMatch(field -> StringUtils.isEmpty(field.trim()))) {

          errors.rejectValue(
              DATA_ENTITLEMENTS,
              "synkrato.services.partner.product.attributes.notEmpty",
              new Object[] {CONDITIONS_REQUIRED},
              EMPTY_STRING);
        }
        validateDuplicateRequiredFields(conditionDto, errors);
        validateAllRequiredFieldsExist(request, conditionDto, errors);
      }
    }
  }

  /**
   * Checks any duplicate required fields
   *
   * @param conditionDto condition object
   * @param errors list of errors
   */
  private void validateDuplicateRequiredFields(ConditionDTO conditionDto, Errors errors) {
    Set<String> duplicateIds = new HashSet<>();
    conditionDto.getRequired().stream()
        .filter(field -> !StringUtils.isEmpty(field.trim()) && !duplicateIds.add(field.trim()))
        .forEach(
            duplicateField ->
                errors.rejectValue(
                    DATA_ENTITLEMENTS,
                    "synkrato.services.partner.product.entitlements.data.conditions.duplicate.found",
                    new Object[] {duplicateField},
                    EMPTY_STRING));
  }

  /**
   * Checks all the conditions[] required fields exist in request manifest
   *
   * @param request request object
   * @param errors list of errors
   */
  private void validateAllRequiredFieldsExist(
      PlmAttributeDef request, ConditionDTO conditionDto, Errors errors) {
    List<String> manifestFields =
        request.getFields().stream().map(FieldDTO::getJsonPath).collect(toList());
    conditionDto.getRequired().stream()
        .filter(
            field -> !StringUtils.isEmpty(field.trim()) && !manifestFields.contains(field.trim()))
        .forEach(
            missingField ->
                errors.rejectValue(
                    DATA_ENTITLEMENTS,
                    "synkrato.services.partner.product.entitlements.data.conditions.required.fields.not-found",
                    new Object[] {missingField},
                    EMPTY_STRING));
  }

  /**
   * Validate Response Data entitlements
   *
   * @param transactionEntitlementDTO Input transactionEntitlementDTO
   * @param errors errors
   */
  private void validateResponseDataEntitlements(
      TransactionEntitlementDTO transactionEntitlementDTO, Errors errors) {
    if (Objects.isNull(transactionEntitlementDTO.getResponse())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.data.required",
          new Object[] {"Response"},
          EMPTY_STRING);
    } else {

      if (Objects.isNull(transactionEntitlementDTO.getResponse().getFields())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.data.entitlement.field.required",
            new Object[] {"Response"},
            EMPTY_STRING);
      }

      if (Objects.nonNull(transactionEntitlementDTO.getResponse().getConditions())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.data.conditions.not-supported",
            new Object[] {"response"},
            EMPTY_STRING);
      }
    }
  }

  /**
   * Validates empty Webhook for ASYNC Integration
   *
   * @param productDTO Product Details
   * @param errors Errors
   */
  private void validateEmptyWebhook(ProductDTO productDTO, Errors errors) {
    if (IntegrationType.ASYNC.equals(productDTO.getIntegrationType())
        && CollectionUtils.isEmpty(productDTO.getWebhooks())) {
      errors.rejectValue(WEBHOOK, "synkrato.services.partner.product.webhook.required");
    }

    if (IntegrationType.P2P.equals(productDTO.getIntegrationType())
        && Objects.nonNull(productDTO.getWebhooks())) {
      errors.rejectValue(WEBHOOK, "synkrato.services.partner.product.webhook.not-required");
    }
  }

  /** Validates the incoming extension limit */
  private void validateExtensionLimit(ProductDTO productDTO, Errors errors) {

    if (Objects.nonNull(productDTO.getExtensionLimit())
        && (productDTO.getExtensionLimit() < 0
            || productDTO.getExtensionLimit() > MAX_EXTENSION_LIMIT)) {
      errors.rejectValue(
          EXTENSION_LIMIT,
          "synkrato.services.partner.product.invalid.extensionLimit",
          new Object[] {MAX_EXTENSION_LIMIT},
          EMPTY_STRING);
    }
  }

  /**
   * Validate JsonPath from data Entitlements (origin, Transaction - request, response)
   *
   * @param manifestDTO manifest Details
   * @param errors Errors
   */
  public void validateDataEntitlementJsonPaths(ManifestDTO manifestDTO, Errors errors) {

    Set<String> jsonPathSet = getJsonPaths(manifestDTO.getOrigin());
    jsonPathSet.addAll(getTransactionJsonPaths(manifestDTO.getTransactions()));

    Set<String> invalidJsonPathSet = new HashSet<>();
    Set<String> vectorJsonPathSet = new HashSet<>();

    if (jsonPathSet.contains(null) || jsonPathSet.contains(EMPTY_STRING)) {
      errors.rejectValue(
          DATA_ENTITLEMENTS, "synkrato.services.partner.product.entitlements.data.json-path.empty");
    } else {
      jsonPathSet.stream()
          .forEach(
              jsonPath -> {
                try {
                  jsonContext.read(jsonPath);
                } catch (InvalidPathException ex) {
                  invalidJsonPathSet.add(jsonPath);
                }

                // validate if any vector json paths
                if (jsonPath.endsWith(CLOSING_SQUARE_BRACKET)) {
                  vectorJsonPathSet.add(jsonPath);
                }
              });
    }

    if (!CollectionUtils.isEmpty(invalidJsonPathSet)) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.data.json-path.invalid",
          new Object[] {String.join(",", invalidJsonPathSet)},
          EMPTY_STRING);
    }

    if (!CollectionUtils.isEmpty(vectorJsonPathSet)) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.data.scalar-json-path.invalid",
          new Object[] {String.join(", ", vectorJsonPathSet)},
          EMPTY_STRING);
    }
  }

  /**
   * get JsonPath from data Entitlements (origin)
   *
   * @param plmAttributeDef manifest Details
   * @return set of incoming origin jsonPaths
   */
  public Set<String> getJsonPaths(PlmAttributeDef plmAttributeDef) {

    Set<String> jsonPathSet = new HashSet<>();

    if (Objects.nonNull(plmAttributeDef) && !CollectionUtils.isEmpty(plmAttributeDef.getFields())) {
      jsonPathSet.addAll(
          plmAttributeDef.getFields().stream()
              .map(field -> !ObjectUtils.isEmpty(field.jsonPath) ? field.jsonPath : "")
              .collect(toList()));
    }

    return jsonPathSet;
  }

  /**
   * Validate JsonPath from data Entitlements (Transaction - request, response)
   *
   * @param transactionEntitlementDtoList List of transaction Entitlements
   * @return set of incoming transaction - request/response jsonPaths by appending to input
   *     parameter
   */
  private Set<String> getTransactionJsonPaths(
      List<TransactionEntitlementDTO> transactionEntitlementDtoList) {

    Set<String> jsonPathSet = new HashSet<>();

    if (!CollectionUtils.isEmpty(transactionEntitlementDtoList)) {
      transactionEntitlementDtoList.stream()
          .forEach(
              transactionEntitlementDto -> {
                jsonPathSet.addAll(getJsonPaths(transactionEntitlementDto.getRequest()));
                jsonPathSet.addAll(getJsonPaths(transactionEntitlementDto.getResponse()));
              });
    }

    return jsonPathSet;
  }

  /**
   * Validates incoming allow and deny list in access entitlement
   *
   * @param productDTO input product
   * @param errors input binding errors
   */
  private void validateAccessEntitlements(ProductDTO productDTO, Errors errors) {

    if (Objects.nonNull(productDTO.getEntitlements())
        && Objects.nonNull(productDTO.getEntitlements().getAccess())) {
      AccessEntitlementDTO access = productDTO.getEntitlements().getAccess();

      if (CommonUtil.isAnyObjectNull(access.getAllow())
          || CommonUtil.isAnyObjectNull(access.getDeny())) {
        errors.rejectValue(
            ACCESS_ENTITLEMENTS, "synkrato.services.partner.product.entitlements.access.invalid");
      }

      validateAllowDeny(access, errors);
    }
  }

  /**
   * Validates the incoming patterns against the database.
   *
   * @param access access entitlements
   * @param errors input binding errors
   */
  private void validateAllowDeny(AccessEntitlementDTO access, Errors errors) {

    try {
      if (!CollectionUtils.isEmpty(access.getAllow())) {
        access.getAllow().forEach(productService::validateRegex);
      }

      if (!CollectionUtils.isEmpty(access.getDeny())) {
        access.getDeny().forEach(productService::validateRegex);
      }
    } catch (DataIntegrityViolationException ex) {
      errors.rejectValue(
          ACCESS_ENTITLEMENTS, "synkrato.services.partner.product.entitlements.access.invalid");
    }
  }

  /**
   * Validate results section for action and formats
   *
   * @param manifestDTO
   * @param errors
   */
  private void validateManifestResults(ManifestDTO manifestDTO, Errors errors) {
    if (!CollectionUtils.isEmpty(manifestDTO.getTransactions())) {
      manifestDTO
          .getTransactions()
          .forEach(
              transactionEntitlementDTO ->
                  validateResponseResults(transactionEntitlementDTO, errors));
    }
  }

  /**
   * perform validation on response results
   *
   * @param transactionEntitlementDTO incoming entitlement dto
   * @param errors will contain errors if any
   */
  private void validateResponseResults(
      TransactionEntitlementDTO transactionEntitlementDTO, Errors errors) {

    if (Objects.nonNull(transactionEntitlementDTO.getResponse())
        && !Objects.isNull(transactionEntitlementDTO.getResponse().getResults())) {

      // results can not be empty array if it presents.
      if (CollectionUtils.isEmpty(transactionEntitlementDTO.getResponse().getResults())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.results.action.required");
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.results.formats.required");
      } else {
        // action cannot be duplicate with in the same request type
        validateDuplicateAction(transactionEntitlementDTO.getResponse().getResults(), errors);

        transactionEntitlementDTO
            .getResponse()
            .getResults()
            .forEach(resultDTO -> validateResultsFormats(resultDTO, errors));
      }
    }
  }

  /**
   * Validate results formats by calling S3 cache method to verify format value is available
   *
   * @param resultDTO result dto which we are going to perform format name check
   * @param errors errors details
   */
  private void validateResultsFormats(ResultDTO resultDTO, Errors errors) {

    // formats list can not be null or empty
    if (CollectionUtils.isEmpty(resultDTO.getFormats())) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.results.formats.required");
    } else {
      List<String> keysList = s3ClientCache.getS3ObjectKeyNames(bucketName, prefix);
      List<String> notPresent = new ArrayList<>(resultDTO.getFormats());
      notPresent.removeAll(keysList);
      notPresent.forEach(
          format ->
              errors.rejectValue(
                  DATA_ENTITLEMENTS,
                  "synkrato.services.partner.product.entitlements.results.formats.notFound",
                  new Object[] {format},
                  EMPTY_STRING));

      if (resultDTO.getFormats().size() != resultDTO.getFormats().stream().distinct().count()) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.data.transactions.resource.attribute.duplicate",
            new Object[] {FORMATS},
            EMPTY_STRING);
      }
    }
  }

  /**
   * validate duplicate action received from results array for same requestType
   *
   * @param results response results object which contains action
   * @param errors add validation error
   */
  private void validateDuplicateAction(List<ResultDTO> results, Errors errors) {

    boolean hasEmptyAction =
        results.stream().anyMatch(resultDTO -> StringUtils.isEmpty(resultDTO.getAction()));

    if (hasEmptyAction) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.results.action.required");
    } else if (results.size() > 1) {
      List<String> listAction = results.stream().map(ResultDTO::getAction).collect(toList());
      if (listAction.size()
          != listAction.stream().map(String::toLowerCase).collect(Collectors.toSet()).size()) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            "synkrato.services.partner.product.entitlements.results.action.duplicate");
      }
    }
  }

  /**
   * This method will validate the feature flags defined in the product*
   *
   * @param featureDTO incoming features from the request
   * @param errors validation errors
   */
  private void validateProductFeatures(FeatureDTO featureDTO, Errors errors) {
    log.debug("validate_product_features {}", LOGGER_START);

    if (Objects.isNull(featureDTO)) {
      return;
    }

    // if sendFindings is false or null then receiveAutomatedFindingUpdates is not allowed
    if (Boolean.TRUE.equals(featureDTO.getReceiveAutomatedFindingUpdates())
        && (Objects.isNull(featureDTO.getSendFindings())
            || Boolean.FALSE.equals(featureDTO.getSendFindings()))) {
      errors.rejectValue(
          FEATURE,
          "synkrato.services.partner.product.entitlements.features.invalid.finding.feature");
    }

    log.debug("validate_product_features {}", LOGGER_END);
  }

  /**
   * This method will validate FindingTypes
   *
   * @param findingTypes
   * @param errors
   */
  private void validateFindingTypes(List<FindingTypeDTO> findingTypes, Errors errors) {

    if (CollectionUtils.isEmpty(findingTypes)) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.attributes.notEmpty",
          new Object[] {FINDING_TYPE},
          EMPTY_STRING);
      return;
    }

    Set<String> uniqueCodes = new HashSet<>();
    Set<String> duplicates = new HashSet<>();

    for (int i = 0; i < findingTypes.stream().count(); i++) {

      FindingTypeDTO conditionType = findingTypes.get(i);
      // checking if the code is blank
      if (ObjectUtils.isEmpty(conditionType.getCode())
          || ObjectUtils.isEmpty(conditionType.getCode().trim())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            EMPTY_ATTRIBUTE_WITH_INDEX,
            new Object[] {CODE, FINDING_TYPE, i},
            EMPTY_STRING);
      } else {

        conditionType.setCode(conditionType.getCode().trim());

        // checking if the incoming input has *. Wild card * is not supported
        if (ASTERISK.equals(conditionType.getCode())) {
          errors.rejectValue(
              DATA_ENTITLEMENTS,
              "synkrato.services.partner.product.entitlements.invalid.with-index",
              new Object[] {ASTERISK, FINDING_TYPE, i},
              EMPTY_STRING);
        }

        if (!uniqueCodes.add(conditionType.getCode().toLowerCase())) {
          duplicates.add(conditionType.getCode());
        }
      }

      // checking if the name is blank
      if (ObjectUtils.isEmpty(conditionType.getName())
          || ObjectUtils.isEmpty(conditionType.getName().trim())) {
        errors.rejectValue(
            DATA_ENTITLEMENTS,
            EMPTY_ATTRIBUTE_WITH_INDEX,
            new Object[] {NAME, FINDING_TYPE, i},
            EMPTY_STRING);
      }
    }

    if (!duplicates.isEmpty()) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          DUPLICATE_ATTRIBUTE,
          new Object[] {CODE, String.join(", ", duplicates), FINDING_TYPE},
          EMPTY_STRING);
    }
  }

  /**
   * validate statuses received from results array for same requestType
   *
   * @param statuses response results object which contains statuses
   * @param statusType statuses or outboundStatuses
   * @param errors add validation error
   */
  private void validateStatuses(List<String> statuses, String statusType, Errors errors) {

    /* statuses is an optional attribute. The reason we are checking for null first and
    Collections.isEmpty inside because the user can send statuses: null or statuses:[]. */
    if (CollectionUtils.isEmpty(statuses)) {
      return;
    }

    Set<String> statusesSet = new HashSet<>();
    Set<String> duplicates = new HashSet<>();
    HashSet<String> existingErrors = new HashSet<>();

    for (int i = 0; i < statuses.size(); i++) {

      if (Objects.nonNull(statuses.get(i))) {
        statuses.set(i, statuses.get(i).trim());
      }

      // Validate the status text
      boolean validStatusText = validateStatus(statuses.get(i), statusType, errors, existingErrors);

      if (validStatusText && !statusesSet.add(statuses.get(i).toLowerCase())) {
        duplicates.add(statuses.get(i));
      }
    }

    if (!duplicates.isEmpty()) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          DUPLICATE_ATTRIBUTE,
          new Object[] {STATUS, String.join(", ", duplicates), statusType},
          EMPTY_STRING);
    }
  }

  /**
   * Validate status text
   *
   * @param status
   * @param errors
   * @param existingErrors
   * @return
   */
  private boolean validateStatus(
      String status, String statusType, Errors errors, HashSet<String> existingErrors) {

    boolean valid = false;

    // Checking if the input is blank
    if (ObjectUtils.isEmpty(status) || ObjectUtils.isEmpty(status.trim())) {
      if (existingErrors.add(EMPTY_ATTRIBUTE + statusType)) {
        errors.rejectValue(
            DATA_ENTITLEMENTS, EMPTY_ATTRIBUTE, new Object[] {statusType}, EMPTY_STRING);
      }
    } else if (ASTERISK.equals(status)) {
      // checking if the incoming input has *. Wild card * is not supported
      if (existingErrors.add(INVALID_FINDING + statusType)) {
        errors.rejectValue(
            DATA_ENTITLEMENTS, INVALID_FINDING, new Object[] {ASTERISK, statusType}, EMPTY_STRING);
      }
    } else {
      // All validations passed
      valid = true;
    }

    return valid;
  }

  /**
   * Validate request and response resources list
   *
   * @param manifestDTO
   * @param errors
   */
  private void validateManifestResources(ManifestDTO manifestDTO, Errors errors) {
    if (!CollectionUtils.isEmpty(manifestDTO.getTransactions())) {
      manifestDTO
          .getTransactions()
          .forEach(
              transactionEntitlementDTO -> {
                validateRequestResources(transactionEntitlementDTO, errors);
                validateResponseResources(transactionEntitlementDTO, errors);
              });
    }
  }

  /**
   * validate resources for request
   *
   * @param transactionEntitlementDTO
   * @param errors
   */
  private void validateRequestResources(
      TransactionEntitlementDTO transactionEntitlementDTO, Errors errors) {

    if (Objects.isNull(transactionEntitlementDTO.getRequest())
        || Objects.isNull(transactionEntitlementDTO.getRequest().getResources())) {
      return;
    }

    validateResources(transactionEntitlementDTO.getRequest().getResources(), errors);
  }

  /**
   * validate resources for response
   *
   * @param transactionEntitlementDTO
   * @param errors
   */
  private void validateResponseResources(
      TransactionEntitlementDTO transactionEntitlementDTO, Errors errors) {

    if (Objects.isNull(transactionEntitlementDTO.getResponse())
        || Objects.isNull(transactionEntitlementDTO.getResponse().getResources())) {
      return;
    }

    validateResources(transactionEntitlementDTO.getResponse().getResources(), errors);
  }

  /**
   * validate resources list.
   *
   * @param resources
   * @param errors
   */
  private void validateResources(List<String> resources, Errors errors) {

    if (resources.isEmpty()) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          "synkrato.services.partner.product.entitlements.results.resources.notEmpty",
          new Object[] {RESOURCES},
          EMPTY_STRING);
    } else {

      Set<String> types = new HashSet<>();
      resources.forEach(
          type -> {
            if (ObjectUtils.isEmpty(type)) {
              errors.rejectValue(
                  DATA_ENTITLEMENTS,
                  "synkrato.services.partner.product.entitlements.data.transactions.resources.notEmpty",
                  new Object[] {type},
                  EMPTY_STRING);
            } else if (!types.add((type))) {
              errors.rejectValue(
                  DATA_ENTITLEMENTS,
                  "synkrato.services.partner.product.entitlements.data.transactions.resource.attribute.duplicate",
                  new Object[] {String.format("%s '%s'", RESOURCE_TYPE, type)},
                  EMPTY_STRING);
            }
          });
    }
  }
}
