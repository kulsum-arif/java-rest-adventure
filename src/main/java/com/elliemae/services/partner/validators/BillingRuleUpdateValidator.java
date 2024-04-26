package com.synkrato.services.partner.validators;

import static com.synkrato.services.epc.common.EpcCommonConstants.EMPTY_STRING;
import static com.synkrato.services.epc.common.EpcCommonConstants.ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;

import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.TransformationDTO;
import com.synkrato.services.epc.common.dto.enums.BillingRuleStatus;
import com.synkrato.services.epc.common.dto.enums.EnvironmentType;
import com.synkrato.services.epc.common.dto.enums.ProductStatusType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Slf4j
@Component
public class BillingRuleUpdateValidator implements Validator {

  private static final String PRODUCT_ID = "productId";
  private static final String SCHEMA_VERSION = "schemaVersion";
  private static final String STATUS = "status";
  private static final String VALIDATE_IF_AUTHORIZED_TO_UPDATE_LOG =
      "validate_if_authorized_to_update {}";
  private static final String TRANSFORMATIONS = "transformations";
  private static final String UPDATE_BILLING_RULE_MAP = "updateBillingRuleMap";
  private static final String EXISTING_BILLING_RULE_DTO = "existingBillingRuleDTO";
  @Autowired private MessageUtil messageUtil;

  @Override
  public boolean supports(Class<?> aClass) {
    return HashMap.class.isAssignableFrom(aClass);
  }

  @Override
  public void validate(Object billingRuleObject, Errors errors) {
    log.debug("validate {}", LOGGER_START);

    Map<String, Object> updateBillingRuleMap =
        (Map) ((Map) billingRuleObject).get(UPDATE_BILLING_RULE_MAP);
    BillingRuleDTO existingBillingRuleDTO =
        (BillingRuleDTO) ((Map) billingRuleObject).get(EXISTING_BILLING_RULE_DTO);

    validateStatus(updateBillingRuleMap, existingBillingRuleDTO, errors);
    validateTransformations(updateBillingRuleMap, errors);
    validateReadonlyAttributes(updateBillingRuleMap, existingBillingRuleDTO, errors);

    log.debug("validate {}", LOGGER_END);
  }

  /**
   * This method will validate the incoming billing rule
   *
   * @param updateBillingRuleMap the updated billing rule object
   * @param existingBillingRuleDTO the existing billing rule objects
   * @param errors list of errors if found
   */
  private void validateReadonlyAttributes(
      Map<String, Object> updateBillingRuleMap,
      BillingRuleDTO existingBillingRuleDTO,
      Errors errors) {
    log.debug("validate_readonly_attributes {}", LOGGER_START);

    // 1. Validating billingRuleId. billingRuleId cannot be deleted/modified
    if (updateBillingRuleMap.containsKey(ID)
        && (Objects.isNull(updateBillingRuleMap.get(ID))
            || !existingBillingRuleDTO.getId().equals(updateBillingRuleMap.get(ID).toString()))) {
      addError(ID, errors);
    }

    // 2. Validating productId. productId cannot be deleted/modified
    if (updateBillingRuleMap.containsKey(PRODUCT_ID)
        && (Objects.isNull(updateBillingRuleMap.get(PRODUCT_ID))
            || !existingBillingRuleDTO
                .getProductId()
                .equals(updateBillingRuleMap.get(PRODUCT_ID).toString()))) {
      addError(PRODUCT_ID, errors);
    }

    // 3. Validating schemaVersion. schemaVersion cannot be deleted/modified
    if (updateBillingRuleMap.containsKey(SCHEMA_VERSION)
        && (Objects.isNull(updateBillingRuleMap.get(SCHEMA_VERSION))
            || !existingBillingRuleDTO
                .getSchemaVersion()
                .equals(updateBillingRuleMap.get(SCHEMA_VERSION)))) {
      addError(SCHEMA_VERSION, errors);
    }

    log.debug("validate_readonly_attributes {}", LOGGER_END);
  }

  /**
   * This method will validate status of the updated billing rule
   *
   * @param updateBillingRuleMap the updated billing rule object
   * @param existingBillingRuleDTO the existing billing rule objects
   * @param errors list of errors if found
   */
  private void validateStatus(
      Map<String, Object> updateBillingRuleMap,
      BillingRuleDTO existingBillingRuleDTO,
      Errors errors) {
    log.debug("validate_status {}", LOGGER_START);

    // Validating status.
    // Partner cannot change the status of a billing rule.

    if (updateBillingRuleMap.containsKey(STATUS)) {

      // Checking for null status here
      if (Objects.isNull(updateBillingRuleMap.get(STATUS))
          || !Arrays.stream(BillingRuleStatus.values())
              .anyMatch(
                  item ->
                      item.getValue()
                          .equalsIgnoreCase(updateBillingRuleMap.get(STATUS).toString()))) {
        errors.rejectValue(
            STATUS, "synkrato.services.partner.product.billingrule.status.update.invalid");
        return;
      }

      // checking for status being updated by any other user here. Only an admin can update the
      // status
      if (!CommonUtil.hasInternalAdminToken()
          && (Objects.isNull(updateBillingRuleMap.get(STATUS))
              || !existingBillingRuleDTO
                  .getStatus()
                  .getValue()
                  .equalsIgnoreCase(updateBillingRuleMap.get(STATUS).toString()))) {

        errors.rejectValue(
            STATUS,
            "synkrato.services.partner.product.readonly",
            new Object[] {STATUS},
            EMPTY_STRING);
      }
    }

    log.debug("validate_status {}", LOGGER_END);
  }

  /**
   * This method will validate the incoming billing rule map for invalid transformations
   *
   * @param updateBillingRuleMap incoming billingRule map
   * @param errors List of errors
   */
  private void validateTransformations(Map<String, Object> updateBillingRuleMap, Errors errors) {
    log.debug("validate_transformations {}", LOGGER_START);

    if (updateBillingRuleMap.containsKey(TRANSFORMATIONS)) {

      // if the transformations object is not a list
      if (!(updateBillingRuleMap.get(TRANSFORMATIONS) instanceof List)) {
        errors.rejectValue(
            TRANSFORMATIONS,
            "synkrato.services.partner.product.billingrule.transformations.content.update.invalid");
        return;
      }

      // if the transformations object is a list and it is empty or null
      if (CollectionUtils.isEmpty(
          (List<TransformationDTO>) updateBillingRuleMap.get(TRANSFORMATIONS))) {
        errors.rejectValue(
            TRANSFORMATIONS,
            "synkrato.services.partner.product.billingrule.transformations.update.invalid");
      }
    }

    log.debug("validate_transformations {}", LOGGER_END);
  }

  /**
   * This method will validate if the user is authorized to update the billing rule
   *
   * @param product product on which the billing rule has been created
   */
  public void validateIfAuthorizedToUpdate(ProductDTO product) {
    log.debug(VALIDATE_IF_AUTHORIZED_TO_UPDATE_LOG, LOGGER_START);
    boolean result = false;

    // if product status is deprecated then we throw a Forbidden error since billing rule cannot be
    // created on a deprecated product
    if (ProductStatusType.deprecated.equals(product.getStatus())) {
      throw new EpcRuntimeException(
          HttpStatus.FORBIDDEN,
          messageUtil.getMessage("synkrato.services.partner.product.billing.status.deprecated"));
    }

    if (CommonUtil.hasInternalAdminToken()) {
      log.info(VALIDATE_IF_AUTHORIZED_TO_UPDATE_LOG, "Internal Admin");
      result = true;
    } else if (CommonUtil.hasPartnerToken()
        && CommonUtil.isPartnerAuthorized(product.getPartnerId())
        && isEnvironmentAuthorized(product.getEnvironment().getDescription())) {

      log.info(VALIDATE_IF_AUTHORIZED_TO_UPDATE_LOG, "valid partner");
      result = true;
    }

    if (!result) {
      throw new EpcRuntimeException(
          HttpStatus.FORBIDDEN, messageUtil.getMessage("synkrato.services.epc.forbidden.error"));
    }

    log.debug(VALIDATE_IF_AUTHORIZED_TO_UPDATE_LOG, LOGGER_END);
  }

  /**
   * checks whether the product and instance environment are authorized Sandbox instance should be
   * able to place txn for approved product
   *
   * @param productEnvironment product environment
   * @return true or false
   */
  private boolean isEnvironmentAuthorized(String productEnvironment) {
    log.debug("is_environment_authorized {}", LOGGER_START);

    String jwtTokenEnv = CommonUtil.getProductEnvironment();

    log.info(
        "is_environment_authorized product_env={}, jwt_token_env={}",
        productEnvironment,
        jwtTokenEnv);

    if (jwtTokenEnv.equals(productEnvironment)
        || (EnvironmentType.sandbox.name().equals(jwtTokenEnv)
            && EnvironmentType.prod.name().equals(productEnvironment))) {
      return true;
    }

    log.debug("is_environment_authorized {}", LOGGER_END);
    return false;
  }

  /**
   * throws 400 bad request on any readonly attributes
   *
   * @param field exception on which field
   */
  private void addError(String field, Errors errors) {
    errors.rejectValue(
        field, "synkrato.services.partner.product.readonly", new Object[] {field}, EMPTY_STRING);
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
