package com.synkrato.services.partner.validators;

import static com.synkrato.services.epc.common.EpcCommonConstants.EMPTY_STRING;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;
import static com.synkrato.services.epc.common.EpcCommonConstants.REQUEST_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.DEFAULT_SCHEMA_VERSION;
import static com.synkrato.services.partner.PartnerServiceConstants.TRANSFORMATTIONS;

import com.amazonaws.services.s3.AmazonS3;
import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.TransformationDTO;
import com.synkrato.services.epc.common.dto.enums.ProductStatusType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.JsonSchemaValidationUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.BillingRuleService;
import com.synkrato.services.partner.cache.S3ClientCache;
import com.synkrato.services.partner.util.BillingRuleUtil;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Slf4j
@Component
public class BillingRuleValidator implements Validator {

  private static final String OPTIONS = "options";
  private static final String VALIDATION_MESSAGE = "validationMessage";
  private static final String REQUEST_TYPES = "requestTypes";
  @Autowired private MessageUtil messageUtil;
  @Autowired private BillingRuleService billingRuleService;
  @Autowired private BillingRuleUtil billingRuleUtil;
  @Autowired private S3ClientCache s3ClientCache;
  @Autowired private AmazonS3 amazonS3;
  @Autowired private JsonSchemaValidationUtil jsonSchemaValidationUtil;

  private static final String GROUP_AS = "groupAs";

  @Value("${synkrato.aws.s3.schema.name}")
  private String bucketName;

  @Value("${synkrato.aws.s3.schema.billing-prefix}")
  private String prefix;

  @Autowired private S3ClientCache s3client;

  @Override
  public boolean supports(Class<?> aClass) {
    return BillingRuleDTO.class.equals(aClass);
  }

  @Override
  public void validate(Object billingRule, Errors errors) {
    log.debug("billing_rule_validator {}", LOGGER_START);

    BillingRuleDTO billingRuleDTO = (BillingRuleDTO) billingRule;

    if (!CollectionUtils.isEmpty(billingRuleDTO.getTransformations())) {

      // validates against schema
      validatePayload(billingRuleDTO, errors);

      // checks for unique sku's
      validateUniqueSkuInTransformations(billingRuleDTO, errors);
    }

    log.debug("billing_rule_validator Validator {}", LOGGER_END);
  }

  /**
   * This method will validate the incoming payload with a Json schema
   *
   * @param billingRule incoming billing rule DTO
   * @param errors Errors
   */
  private void validatePayload(BillingRuleDTO billingRule, Errors errors) {
    log.debug("validate_payload {}", LOGGER_START);

    BillingRuleDTO billingRuleCopy =
        BillingRuleDTO.builder().transformations(billingRule.getTransformations()).build();

    JSONObject transformations = new JSONObject(billingRuleCopy);
    String schemaVersion =
        ObjectUtils.isEmpty(billingRule.getSchemaVersion())
            ? DEFAULT_SCHEMA_VERSION
            : billingRule.getSchemaVersion();
    JSONObject jsonSchema = s3client.getS3ObjectContent(bucketName, prefix, schemaVersion);

    try {
      jsonSchemaValidationUtil.validateJsonSchema(transformations, jsonSchema);
    } catch (ValidationException validationException) {
      log.error(
          "Schema validation failed {}", validationException.getMessage(), validationException);

      Set<String> errorMessages = new HashSet<>();

      Object globalValidationMessageObject =
          jsonSchemaValidationUtil.getValueFromSchema(jsonSchema, VALIDATION_MESSAGE);

      if (Objects.nonNull(globalValidationMessageObject)
          && globalValidationMessageObject instanceof Map) {
        jsonSchemaValidationUtil.buildErrorMessages(
            validationException,
            errorMessages,
            (Map<String, Object>) globalValidationMessageObject);
      } else {
        jsonSchemaValidationUtil.buildErrorMessages(
            validationException, errorMessages, new HashMap<>());
      }

      errorMessages.forEach(
          em ->
              errors.rejectValue(
                  TRANSFORMATTIONS,
                  "synkrato.services.partner.product.billing.transformations.invalid",
                  new Object[] {em},
                  EMPTY_STRING));
    }

    log.debug("validate_payload {}", LOGGER_END);
  }

  /**
   * This method will check if the sku's defined at the root level of each billing transformation is
   * unique and is not duplicated
   *
   * @param billingRule incoming request containing billing rule information
   * @param errors Errors
   */
  private void validateUniqueSkuInTransformations(BillingRuleDTO billingRule, Errors errors) {
    log.debug("validate_unique_sku_in_transformations {}", LOGGER_START);

    List<String> transformationSkus =
        billingRule.getTransformations().stream()
            .filter(Objects::nonNull)
            .map(TransformationDTO::getSku)
            .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(transformationSkus)) {
      log.info("validate_unique_sku_in_transformations no transformations found");
      return;
    }

    for (TransformationDTO transformationDTO :
        billingRule.getTransformations().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList())) {

      validateTransformations(transformationDTO, errors);
    }

    log.debug("validate_unique_sku_in_transformations {}", LOGGER_END);
  }

  /**
   * Validates the option object in the transaction
   *
   * @param transformationDTO transformationDTO object
   * @param errors errors
   */
  private void validateOptionsInTransaction(TransformationDTO transformationDTO, Errors errors) {
    log.debug("validate_options_in_transaction {}", LOGGER_START);

    if (Objects.nonNull(transformationDTO.getTransaction())
        && transformationDTO.getTransaction().containsKey(OPTIONS)
        && Objects.isNull(transformationDTO.getTransaction().get(OPTIONS))) {
      errors.rejectValue(
          TRANSFORMATTIONS,
          "synkrato.services.partner.product.attributes.notEmpty",
          new Object[] {OPTIONS},
          EMPTY_STRING);
    }

    log.debug("validate_options_in_transaction {}", LOGGER_END);
  }

  /**
   * This method will validate transformations
   *
   * @param transformationDTO incoming transformation
   * @param errors errors
   */
  private void validateTransformations(TransformationDTO transformationDTO, Errors errors) {
    log.debug("validate_transformations {}", LOGGER_START);

    validateOptionsInTransaction(transformationDTO, errors);

    // We only do validations if there are grouping rules present in the transformation
    if (CollectionUtils.isEmpty(transformationDTO.getGroupingRules())
        || CollectionUtils.isEmpty(
            transformationDTO.getGroupingRules().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList()))) {
      return;
    }

    Set<String> uniqueGroupSkus =
        transformationDTO.getGroupingRules().stream()
            .filter(Objects::nonNull)
            .map(
                groupingRule ->
                    (groupingRule.containsKey(GROUP_AS)
                            && Objects.nonNull(groupingRule.get(GROUP_AS)))
                        ? groupingRule.get(GROUP_AS).toString()
                        : "")
            .collect(Collectors.toSet());

    // checking if the sku's defined in the grouping rules are unique and not are repeating
    if (uniqueGroupSkus.size() != transformationDTO.getGroupingRules().size()) {
      errors.rejectValue(
          TRANSFORMATTIONS,
          "synkrato.services.partner.product.billing.transformations.groupingRules.sku.duplicate");
    }

    // checking if the sku at the root level matches the sku's defined in the grouping rules
    if (uniqueGroupSkus.stream()
        .anyMatch(item -> item.equalsIgnoreCase(transformationDTO.getSku()))) {
      errors.rejectValue(
          TRANSFORMATTIONS,
          "synkrato.services.partner.product.billing.transformations.sku.duplicate",
          new Object[] {transformationDTO.getSku()},
          EMPTY_STRING);
    }

    log.debug("validate_transformations {}", LOGGER_END);
  }

  /**
   * This method will validate the product on which the billing record is being created/updated
   *
   * @param productDTO incoming product
   */
  public void validateProduct(ProductDTO productDTO) {
    log.debug("validate_product {}", LOGGER_START);

    // if product status is deprecated then we throw a Forbidden error since billing rule cannot be
    // created on a deprecated product
    if (ProductStatusType.deprecated.equals(productDTO.getStatus())) {
      throw new EpcRuntimeException(
          HttpStatus.FORBIDDEN,
          messageUtil.getMessage("synkrato.services.partner.product.billing.status.deprecated"));
    }

    log.debug("validate_product {}", LOGGER_END);
  }

  /**
   * This method will validate the billing rule against the product. Meaning it will validate if the
   * product is deprecated or not and it will also check if the request types are valid
   *
   * @param productDTO the product on which the billing rule is being created
   * @param billingRuleDTO incoming billingRuleDTO
   */
  public void validateBillingRuleForProduct(ProductDTO productDTO, BillingRuleDTO billingRuleDTO) {
    log.debug("validate_billing_rule_for_product {}", LOGGER_START);

    validateProduct(productDTO);

    Set<String> requestTypes = new HashSet<>(productDTO.getRequestTypes());

    Set<String> invalidRequestTypes =
        validateRequestTypes(requestTypes, billingRuleDTO.getTransformations());

    if (!CollectionUtils.isEmpty(invalidRequestTypes)) {
      throw new EpcRuntimeException(
          HttpStatus.BAD_REQUEST,
          messageUtil.getMessage(
              "synkrato.services.partner.product.billing.transformations.requestType.not-found",
              new Object[] {invalidRequestTypes.toString()}));
    }

    log.debug("validate_billing_rule_for_product {}", LOGGER_END);
  }

  /**
   * This method will validate requestType defined in the billing rule. It is for v1 of the billing
   * schema
   *
   * @param requestTypes the product on which the billing rule is being created
   * @param transformations incoming billingRuleDTO
   * @return
   */
  private Set<String> validateRequestTypes(
      Set<String> requestTypes, List<TransformationDTO> transformations) {
    log.debug("validate_request_types {}", LOGGER_START);

    Set<String> invalidRequestTypes = new HashSet<>();

    for (TransformationDTO transformationDTO : transformations) {

      // If the transformation contains requestType. This is for v1 schema
      if (transformationDTO.getTransaction().containsKey(REQUEST_TYPE)
          && !requestTypes.contains(
              transformationDTO.getTransaction().get(REQUEST_TYPE).toString())) {

        invalidRequestTypes.add(transformationDTO.getTransaction().get(REQUEST_TYPE).toString());
      }

      // If the transformation contains requestTypes. This is for v2 schema
      if (transformationDTO.getTransaction().containsKey(REQUEST_TYPES)
          && !requestTypes.containsAll(
              Arrays.asList(transformationDTO.getTransaction().get(REQUEST_TYPES)))) {

        // converting the requestTypes object to Set
        Set<String> requestTypeSet =
            Optional.ofNullable(
                    (ArrayList<String>) transformationDTO.getTransaction().get(REQUEST_TYPES))
                .map(HashSet::new)
                .orElse(new HashSet<>());

        invalidRequestTypes.addAll(Sets.difference(requestTypeSet, requestTypes));
      }
    }

    log.debug("validate_request_types {}", LOGGER_END);
    return invalidRequestTypes;
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
}
