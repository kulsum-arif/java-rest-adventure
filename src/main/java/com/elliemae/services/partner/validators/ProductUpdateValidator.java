package com.synkrato.services.partner.validators;

import static com.synkrato.services.epc.common.EpcCommonConstants.ALT_TEXT;
import static com.synkrato.services.epc.common.EpcCommonConstants.DESCRIPTION;
import static com.synkrato.services.epc.common.EpcCommonConstants.EMPTY_STRING;
import static com.synkrato.services.epc.common.EpcCommonConstants.ENVIRONMENT;
import static com.synkrato.services.epc.common.EpcCommonConstants.ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.INTEGRATION_TYPE;
import static com.synkrato.services.epc.common.EpcCommonConstants.LIST_ELEMENT;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;
import static com.synkrato.services.epc.common.EpcCommonConstants.NAME;
import static com.synkrato.services.epc.common.EpcCommonConstants.OPTIONS;
import static com.synkrato.services.epc.common.EpcCommonConstants.PARTNER_ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.STATUS;
import static com.synkrato.services.epc.common.EpcCommonConstants.URL;
import static com.synkrato.services.epc.common.EpcCommonConstants.WEBHOOK;
import static com.synkrato.services.partner.PartnerServiceConstants.ACCESS_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.ACCESS_ENTITLEMENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.ACCESS_ENTITLEMENTS_ALLOW;
import static com.synkrato.services.partner.PartnerServiceConstants.ACCESS_ENTITLEMENTS_DENY;
import static com.synkrato.services.partner.PartnerServiceConstants.ADDITIONAL_LINKS;
import static com.synkrato.services.partner.PartnerServiceConstants.ADMIN_INTERFACE_URL;
import static com.synkrato.services.partner.PartnerServiceConstants.ALLOW_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.CREDENTIAL;
import static com.synkrato.services.partner.PartnerServiceConstants.CREDENTIAL_TYPE_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.CREDENTIAL_TYPE_TITLE;
import static com.synkrato.services.partner.PartnerServiceConstants.DATA_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.DATA_ENTITLEMENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.DATA_ENTITLEMENTS_ORIGIN;
import static com.synkrato.services.partner.PartnerServiceConstants.DATA_ENTITLEMENTS_TRANSACTIONS;
import static com.synkrato.services.partner.PartnerServiceConstants.DENY_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.ENTITLEMENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.EXTENSION_LIMIT;
import static com.synkrato.services.partner.PartnerServiceConstants.INTERFACE_URL;
import static com.synkrato.services.partner.PartnerServiceConstants.LISTING_NAME;
import static com.synkrato.services.partner.PartnerServiceConstants.MANIFEST_ORIGIN_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.REQUEST_TYPES_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.RESOURCES;
import static com.synkrato.services.partner.PartnerServiceConstants.SCHEMA;
import static com.synkrato.services.partner.PartnerServiceConstants.TAG_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.TRANSACTIONS;
import static com.synkrato.services.partner.PartnerServiceConstants.TRANSACTIONS_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.TYPE_KEY;

import com.synkrato.services.epc.common.dto.AdditionalLinkDTO;
import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.CredentialDTO;
import com.synkrato.services.epc.common.dto.OptionsDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.TransactionEntitlementDTO;

import com.synkrato.services.epc.common.dto.enums.BillingRuleStatus;
import com.synkrato.services.epc.common.dto.enums.ProductStatusType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.BillingRuleService;
import com.synkrato.services.partner.util.ProductUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
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
public class ProductUpdateValidator implements Validator {

  private static final String REQUIRED_ATTRIBUTE_ERROR =
      "synkrato.services.partner.product.attributes.required";
  private static final String REQUEST_TYPE_ATTRIBUTE_DELETE_ERROR =
      "synkrato.services.partner.product.requestTypes.delete.invalid";

  private static final String MAX_LENGTH_ERROR =
      "synkrato.services.partner.product.attribute.invalid.size";

  private static final String INVALID_REQUEST =
      "synkrato.services.partner.product.invalid.content.error";
  private static final String RESPONSE = "response";

  private static final int MAX_LENGTH = 256;
  private static final int ADDITIONAL_LINK_MAX_LENGTH = 128;
  private static final String TRANSACTIONS_JSONPATH = "$.entitlements.data.transactions";

  private static final Configuration JSON_PATH_CONFIGURATION =
      Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();

  @Autowired private MessageUtil messageUtil;
  @Setter @Autowired private ProductUtil productUtil;
  @Setter @Autowired private ObjectMapper objectMapper;
  @Autowired private BillingRuleService billingRuleService;

  @Override
  public boolean supports(Class<?> aClass) {
    return HashMap.class.isAssignableFrom(aClass);
  }

  /**
   * This method throws FORBIDDEN exception if readonly attributes exist in payload Reason for
   * passing product object 1. Need to compare the values with existing db values 2. Errors work
   * with product class and not with Map
   *
   * @param object object to validate
   * @param errors errors
   */
  @Override
  public void validate(Object object, Errors errors) {
    log.debug("Product update premerge validation {}", LOGGER_START);

    Map<String, Object> updateProductMap = (Map) ((Map) object).get("updateProductMap");
    ProductDTO existingProductDTO = (ProductDTO) ((Map) object).get("existingProductDTO");

    validateAttributesConstraints(updateProductMap, errors);
    validateReadonlyAttributes(updateProductMap, existingProductDTO, errors);
    validateBillingRule(updateProductMap, existingProductDTO, errors);
    validateCredential(updateProductMap, errors);
    validateOptions(updateProductMap, existingProductDTO, errors);
    validateDataEntitlements(updateProductMap, existingProductDTO, errors);
    validateAdditionalLinks(updateProductMap, errors);

    log.debug("Product update premerge validation {}", LOGGER_END);
  }

  /**
   * validate additionalLinks list
   *
   * @param updateProductMap
   * @param errors
   */
  private void validateAdditionalLinks(Map<String, Object> updateProductMap, Errors errors) {
    log.debug("validate_additional_links {}", LOGGER_START);

    if (!updateProductMap.containsKey(ADDITIONAL_LINKS)
        || Objects.isNull(updateProductMap.get(ADDITIONAL_LINKS))) {
      return;
    }
    List<AdditionalLinkDTO> additionalLinkDTOList = new ArrayList<>();
    try {
      additionalLinkDTOList =
          objectMapper.convertValue(
              updateProductMap.get(ADDITIONAL_LINKS),
              new TypeReference<List<AdditionalLinkDTO>>() {});
    } catch (IllegalArgumentException ex) {
      log.error(ex.getMessage());
      throw new EpcRuntimeException(
          HttpStatus.BAD_REQUEST,
          messageUtil.getMessage(INVALID_REQUEST, new Object[] {ADDITIONAL_LINKS}));
    }

    for (int i = 0; i < additionalLinkDTOList.size(); i++) {
      validateAdditionalLink(additionalLinkDTOList.get(i), errors, i);
    }
    log.debug("validate_additional_links {}", LOGGER_END);
  }

  /**
   * validate individual additionalLink
   *
   * @param additionalLinkDTO
   * @param errors
   * @param i
   */
  private void validateAdditionalLink(AdditionalLinkDTO additionalLinkDTO, Errors errors, int i) {
    log.debug("validate_additional_link {}", LOGGER_START);
    // No null value
    if (Objects.isNull(additionalLinkDTO)) {
      throw new EpcRuntimeException(
          HttpStatus.BAD_REQUEST,
          messageUtil.getMessage(INVALID_REQUEST, new Object[] {ADDITIONAL_LINKS}));
    }
    validateType(additionalLinkDTO, errors, i);
    validateDescription(additionalLinkDTO, errors, i);
    validateUrl(additionalLinkDTO, errors, i);
    validateAltText(additionalLinkDTO, errors, i);

    log.debug("validate_additional_link {}", LOGGER_END);
  }

  /**
   * validate that altText is required and its length can not be more than 128
   *
   * @param additionalLinkDTO
   * @param errors
   * @param i
   */
  private void validateAltText(AdditionalLinkDTO additionalLinkDTO, Errors errors, int i) {

    if (ObjectUtils.isEmpty(additionalLinkDTO.getAltText())) {
      errors.rejectValue(
          String.format(LIST_ELEMENT, ADDITIONAL_LINKS, i, ALT_TEXT),
          REQUIRED_ATTRIBUTE_ERROR,
          new Object[] {ALT_TEXT},
          EMPTY_STRING);
    } else if (additionalLinkDTO.getAltText().length() > ADDITIONAL_LINK_MAX_LENGTH) {
      errors.rejectValue(
          String.format(LIST_ELEMENT, ADDITIONAL_LINKS, i, ALT_TEXT),
          MAX_LENGTH_ERROR,
          new Object[] {ADDITIONAL_LINK_MAX_LENGTH},
          EMPTY_STRING);
    }
  }

  /**
   * validate that url is required.
   *
   * @param additionalLinkDTO
   * @param errors
   * @param i
   */
  private void validateUrl(AdditionalLinkDTO additionalLinkDTO, Errors errors, int i) {

    if (ObjectUtils.isEmpty(additionalLinkDTO.getUrl())) {
      errors.rejectValue(
          String.format(LIST_ELEMENT, ADDITIONAL_LINKS, i, URL),
          REQUIRED_ATTRIBUTE_ERROR,
          new Object[] {URL},
          EMPTY_STRING);
    }
  }

  /**
   * validate that description is optional and length can not be more than 128 if presents.
   *
   * @param additionalLinkDTO
   * @param errors
   * @param i
   */
  private void validateDescription(AdditionalLinkDTO additionalLinkDTO, Errors errors, int i) {

    if (!ObjectUtils.isEmpty(additionalLinkDTO.getDescription())
        && additionalLinkDTO.getDescription().length() > ADDITIONAL_LINK_MAX_LENGTH) {
      errors.rejectValue(
          String.format(LIST_ELEMENT, ADDITIONAL_LINKS, i, DESCRIPTION),
          MAX_LENGTH_ERROR,
          new Object[] {ADDITIONAL_LINK_MAX_LENGTH},
          EMPTY_STRING);
    }
  }

  /**
   * validate that type is required.
   *
   * @param additionalLinkDTO
   * @param errors
   * @param i
   */
  private void validateType(AdditionalLinkDTO additionalLinkDTO, Errors errors, int i) {
    if (ObjectUtils.isEmpty(additionalLinkDTO.getType())) {
      errors.rejectValue(
          String.format(LIST_ELEMENT, ADDITIONAL_LINKS, i, TYPE_KEY),
          REQUIRED_ATTRIBUTE_ERROR,
          new Object[] {TYPE_KEY},
          EMPTY_STRING);
    }
  }

  /**
   * Validates attributes constraints
   *
   * @param updateProductMap request patch product payload
   */
  private void validateAttributesConstraints(Map<String, Object> updateProductMap, Errors errors) {
    if (!StringUtils.isEmpty(updateProductMap.get(LISTING_NAME))
        && ((String) updateProductMap.get(LISTING_NAME)).length() > MAX_LENGTH) {
      errors.rejectValue(
          LISTING_NAME,
          "synkrato.services.partner.product.attribute.invalid.size",
          new Object[] {MAX_LENGTH},
          EMPTY_STRING);
    }

    // check non null tags, if provided in payload. Validate any key in the tags is non-null
    if (updateProductMap.containsKey(TAG_ATTRIBUTE)
        && updateProductMap.get(TAG_ATTRIBUTE) instanceof Map) {
      Map<String, List<String>> tags =
          (Map<String, List<String>>) updateProductMap.get(TAG_ATTRIBUTE);

      List<String> nullOrEmptyTags =
          tags.entrySet().stream()
              .filter(
                  entry ->
                      Objects.isNull(entry.getValue()) || CollectionUtils.isEmpty(entry.getValue()))
              .map(Map.Entry::getKey)
              .collect(Collectors.toList());

      if (!CollectionUtils.isEmpty(nullOrEmptyTags)) {
        errors.rejectValue(
            TAG_ATTRIBUTE,
            "synkrato.services.partner.product.attributes.notEmpty",
            new Object[] {nullOrEmptyTags},
            EMPTY_STRING);
      }
    }
  }

  /**
   * Validates when payload has any read only attributes
   *
   * @param updateProductMap incoming product payload
   */
  private void validateReadonlyAttributes(
      Map<String, Object> updateProductMap, ProductDTO existingProductDTO, Errors errors) {

    // product Id - can't be deleted/modified
    if (updateProductMap.containsKey(ID)) {
      if (Objects.isNull(updateProductMap.get(ID))
          || !existingProductDTO.getId().equals(updateProductMap.get(ID).toString())) {
        addError(ID, errors);
      }
    }

    // product name - can't be deleted/modified
    if (updateProductMap.containsKey(NAME)) {
      if (Objects.isNull(updateProductMap.get(NAME))
          || !existingProductDTO.getName().equals(updateProductMap.get(NAME).toString())) {
        addError(NAME, errors);
      }
    }

    // partnerId - can't be deleted/modified
    if (updateProductMap.containsKey(PARTNER_ID)) {
      if (Objects.isNull(updateProductMap.get(PARTNER_ID))
          || !existingProductDTO
              .getPartnerId()
              .equals(updateProductMap.get(PARTNER_ID).toString())) {
        addError(PARTNER_ID, errors);
      }
    }

    // environment - can't be deleted/modified
    if (updateProductMap.containsKey(ENVIRONMENT)) {
      if (Objects.isNull(updateProductMap.get(ENVIRONMENT))
          || !updateProductMap
              .get(ENVIRONMENT)
              .toString()
              .equalsIgnoreCase(existingProductDTO.getEnvironment().getDescription())) {
        addError(ENVIRONMENT, errors);
      }
    }

    /* status - can't be deleted
    partner - can't change the status when status is approved or deprecated
    partner - can't send approved or deprecated
    internal Admin cannot update the status to deprecated */
    if (updateProductMap.containsKey(STATUS)) {
      validateStatusAttribute(updateProductMap, existingProductDTO, errors);
    }
    // listing name - can't be deleted
    // partner can't update prod product listing name
    if (updateProductMap.containsKey(LISTING_NAME)) {
      if (Objects.isNull(updateProductMap.get(LISTING_NAME))) {
        addError(LISTING_NAME, errors);
      } else if (isProductApprovedOrDeprecated(existingProductDTO)
          && CommonUtil.hasPartnerToken()
          && !existingProductDTO
              .getListingName()
              .equals(updateProductMap.get(LISTING_NAME).toString())) {
        addError(LISTING_NAME, errors);
      }
    }

    // requestTypes - can't be deleted
    // partner can't update prod product requestTypes
    if (updateProductMap.containsKey(REQUEST_TYPES_ATTRIBUTE)) {
      if (Objects.isNull(updateProductMap.get(REQUEST_TYPES_ATTRIBUTE))) {
        addError(REQUEST_TYPES_ATTRIBUTE, errors);
      } else if (isProductApprovedOrDeprecated(existingProductDTO)
          && CommonUtil.hasPartnerToken()) {

        List<String> requestTypes = (List<String>) updateProductMap.get(REQUEST_TYPES_ATTRIBUTE);
        if (requestTypes.size() != existingProductDTO.getRequestTypes().size()
            || !requestTypes.equals(existingProductDTO.getRequestTypes())) {
          addError(REQUEST_TYPES_ATTRIBUTE, errors);
        }
      }
    }

    // integrationType can't be deleted
    // Admin  - can't update prod product
    // partner - can't update sandbox or prod product
    if (updateProductMap.containsKey(INTEGRATION_TYPE)) {
      if (Objects.isNull(updateProductMap.get(INTEGRATION_TYPE))) {
        addError(INTEGRATION_TYPE, errors);
      } else if ((isProductApprovedOrDeprecated(existingProductDTO) || CommonUtil.hasPartnerToken())
          && !updateProductMap
              .get(INTEGRATION_TYPE)
              .toString()
              .equalsIgnoreCase(existingProductDTO.getIntegrationType().name())) {
        addError(INTEGRATION_TYPE, errors);
      }
    }

    // entitlements
    if (updateProductMap.containsKey(ENTITLEMENTS)) {
      try {
        validateEntitlement((Map) updateProductMap.get(ENTITLEMENTS), existingProductDTO, errors);
      } catch (ClassCastException ex) {
        errors.rejectValue(
            ENTITLEMENTS,
            "synkrato.services.partner.product.invalid.content.error",
            new Object[] {ENTITLEMENTS},
            EMPTY_STRING);
      }
    }

    // extensionLimit can't be deleted.
    // partner - can't update extension limit.
    if (hasExtensionLimit(updateProductMap)) {
      if (CommonUtil.hasPartnerToken()) {
        addError(EXTENSION_LIMIT, errors);
      } else if (isDeleteExtensionLimit(updateProductMap)) {
        errors.rejectValue(
            EXTENSION_LIMIT,
            REQUIRED_ATTRIBUTE_ERROR,
            new Object[] {EXTENSION_LIMIT},
            EMPTY_STRING);
      } else if (isEmptyExtensionLimit(updateProductMap)) {
        errors.rejectValue(
            EXTENSION_LIMIT,
            "synkrato.services.partner.product.invalid.content.error",
            new Object[] {EXTENSION_LIMIT},
            EMPTY_STRING);
      }
    }

    // tags - can be deleted
    // partner can't update prod product tags
    if (updateProductMap.containsKey(TAG_ATTRIBUTE)) {
      if (isProductApprovedOrDeprecated(existingProductDTO) && CommonUtil.hasPartnerToken()) {
        addError(TAG_ATTRIBUTE, errors);
      }
    }

    // credentials - can be deleted
    // partner can't update prod product credentials
    if (updateProductMap.containsKey(CREDENTIAL)) {
      if (isProductApprovedOrDeprecated(existingProductDTO) && CommonUtil.hasPartnerToken()) {
        addError(CREDENTIAL, errors);
      }
    }

    // options - can be deleted
    // partner can't update approved/prod product options
    if (updateProductMap.containsKey(OPTIONS)
        && CommonUtil.hasPartnerToken()
        && isProductApprovedOrDeprecated(existingProductDTO)) {
      addError(OPTIONS, errors);
    }

    // partner can't update approved/prod product interfaceUrl
    if (CommonUtil.hasPartnerToken()
        && updateProductMap.containsKey(INTERFACE_URL)
        && isProductApprovedOrDeprecated(existingProductDTO)) {
      addError(INTERFACE_URL, errors);
    }

    // partner can't update approved/prod product adminInterfaceUrl
    if (CommonUtil.hasPartnerToken()
        && updateProductMap.containsKey(ADMIN_INTERFACE_URL)
        && isProductApprovedOrDeprecated(existingProductDTO)) {
      addError(ADMIN_INTERFACE_URL, errors);
    }

    // partner can't update approved/prod product webhooks
    if (CommonUtil.hasPartnerToken()
        && updateProductMap.containsKey(WEBHOOK)
        && isProductApprovedOrDeprecated(existingProductDTO)) {
      addError(WEBHOOK, errors);
    }

    // Biz dev user is allowed only to update the status to deprecated
    if (CommonUtil.isBizDevUser()
        && !CommonUtil.hasInternalAdminToken()
        && (isValidContentForBizDevUser(updateProductMap))) {
      throw new EpcRuntimeException(
          HttpStatus.BAD_REQUEST,
          messageUtil.getMessage("synkrato.services.partner.product.status.update.error"));
    }
  }

  /**
   * This method will validate if user is allowed to update the status with the value given based on
   * the user role
   *
   * @param updateProductMap incoming product map
   * @param existingProductDTO existing product
   * @param errors throws an error if user is not allowed to update the status with the value given
   */
  private void validateStatusAttribute(
      Map<String, Object> updateProductMap, ProductDTO existingProductDTO, Errors errors) {
    log.debug("validate_status_attribute {} ", LOGGER_START);

    if (Objects.isNull(updateProductMap.get(STATUS))) {
      addError(STATUS, errors);
    } else if (CommonUtil.hasPartnerToken()
        && !updateProductMap
            .get(STATUS)
            .toString()
            .equalsIgnoreCase(existingProductDTO.getStatus().name())) {

      if (isProductApprovedOrDeprecated(existingProductDTO)) {
        addError(STATUS, errors);
      } else if (isUpdatedProductApprovedOrDeprecated(updateProductMap)) {
        addError(STATUS, errors);
      }
    } else if (canUpdateDeprecated(existingProductDTO, updateProductMap)) {
      addError(STATUS, errors);
    }

    log.debug("validate_status_attribute {} ", LOGGER_END);
  }

  /**
   * This method will check if Bizdev user is allowed to update the content
   *
   * @param updateProductMap incoming product map
   */
  private boolean isValidContentForBizDevUser(Map<String, Object> updateProductMap) {
    return updateProductMap.size() > 1
        || (updateProductMap.size() == 1
            && (!updateProductMap.containsKey(STATUS)
                || !updateProductMap
                    .get(STATUS)
                    .toString()
                    .equalsIgnoreCase(ProductStatusType.deprecated.getDescription())));
  }

  /**
   * This method will validate if there is an existing billing rule in approved state before
   * approving a product
   *
   * @param updateProductMap incoming product map
   * @param existingProductDTO existing product
   * @param errors throws an error if a product does not have an approved billing rule
   */
  private void validateBillingRule(
      Map<String, Object> updateProductMap, ProductDTO existingProductDTO, Errors errors) {
    log.debug("validate_billing_rule {} ", LOGGER_START);

    if (Objects.nonNull(updateProductMap.get(STATUS))
        && CommonUtil.hasInternalAdminToken()
        && ProductStatusType.approved
            .getDescription()
            .equalsIgnoreCase(updateProductMap.get(STATUS).toString())) {

      List<BillingRuleDTO> billingRules =
          billingRuleService.findBillingRules(existingProductDTO.getId());

      // If billing rules is empty or billing rules is not empty and there is no billing rule in
      // approved state.
      if (CollectionUtils.isEmpty(billingRules)
          || (!CollectionUtils.isEmpty(billingRules)
              && !billingRules.stream()
                  .anyMatch(
                      billingRule -> BillingRuleStatus.APPROVED.equals(billingRule.getStatus())))) {
        errors.rejectValue(
            STATUS, "synkrato.services.partner.product.billingrule.approved.forbidden");
      }
    }

    log.debug("validate_billing_rule {} ", LOGGER_END);
  }

  /**
   * validates access and data entitlements
   *
   * @param entitlementsMap update product entitlements map payload
   * @param existingProductDTO existing product
   * @param errors error collection
   */
  private void validateEntitlement(
      Map<String, Object> entitlementsMap, ProductDTO existingProductDTO, Errors errors) {

    if (Objects.isNull(entitlementsMap)) {
      errors.rejectValue(
          ENTITLEMENTS, REQUIRED_ATTRIBUTE_ERROR, new Object[] {ENTITLEMENTS}, EMPTY_STRING);
    } else {
      if (entitlementsMap.containsKey(DATA_ATTRIBUTE)) {
        validateDataEntitlement(entitlementsMap.get(DATA_ATTRIBUTE), errors);
      }
      if (entitlementsMap.containsKey(ACCESS_ATTRIBUTE)) {
        validateAccessEntitlement(
            entitlementsMap.get(ACCESS_ATTRIBUTE), existingProductDTO, errors);
      }
    }
  }

  /**
   * validates AccessEntitlement
   *
   * @param accessEntitlementsMap incoming entitlements Map
   * @param existingProductDTO existing product
   * @param errors error collection
   */
  private void validateAccessEntitlement(
      Object accessEntitlementsMap, ProductDTO existingProductDTO, Errors errors) {

    if (isProductApprovedOrDeprecated(existingProductDTO) && CommonUtil.hasPartnerToken()) {
      addError(ACCESS_ENTITLEMENTS, errors);
    } else if (isDeleteAccessEntitlement(accessEntitlementsMap)) {
      errors.rejectValue(
          ACCESS_ENTITLEMENTS,
          REQUIRED_ATTRIBUTE_ERROR,
          new Object[] {ACCESS_ENTITLEMENTS},
          EMPTY_STRING);
    } else {
      if (hasAccessEntitlementAllow(accessEntitlementsMap)
          && isDeleteAccessEntitlementAllow(accessEntitlementsMap)) {
        errors.rejectValue(
            ACCESS_ENTITLEMENTS_ALLOW,
            REQUIRED_ATTRIBUTE_ERROR,
            new Object[] {ACCESS_ENTITLEMENTS_ALLOW},
            EMPTY_STRING);
      }

      if (hasAccessEntitlementDeny(accessEntitlementsMap)
          && isDeleteAccessEntitlementDeny(accessEntitlementsMap)) {
        errors.rejectValue(
            ACCESS_ENTITLEMENTS_DENY,
            REQUIRED_ATTRIBUTE_ERROR,
            new Object[] {ACCESS_ENTITLEMENTS_DENY},
            EMPTY_STRING);
      }
    }
  }

  /**
   * validates DataEntitlement
   *
   * @param dataEntitlementsMap incoming data entitlements Map
   * @param errors error collection
   */
  private void validateDataEntitlement(Object dataEntitlementsMap, Errors errors) {

    if (CommonUtil.hasPartnerToken() && Objects.nonNull(dataEntitlementsMap)) {
      Map dataEntitlements = (Map) dataEntitlementsMap;
      if (dataEntitlements.containsKey(MANIFEST_ORIGIN_TYPE)) {
        addError(DATA_ENTITLEMENTS_ORIGIN, errors);
      }
      if (dataEntitlements.containsKey(TRANSACTIONS_ATTRIBUTE)) {
        addError(DATA_ENTITLEMENTS_TRANSACTIONS, errors);
      }
    } else if (isDeleteDataEntitlement(dataEntitlementsMap)) {
      errors.rejectValue(
          DATA_ENTITLEMENTS,
          REQUIRED_ATTRIBUTE_ERROR,
          new Object[] {DATA_ENTITLEMENTS},
          EMPTY_STRING);
    }
  }

  private boolean isDeleteDataEntitlement(Object dataEntitlementMap) {
    return Objects.isNull(dataEntitlementMap);
  }

  private boolean isDeleteAccessEntitlement(Object accessEntitlementMap) {
    return Objects.isNull(accessEntitlementMap);
  }

  private boolean isDeleteExtensionLimit(Object updateProductMap) {
    return Objects.isNull(((Map) updateProductMap).get(EXTENSION_LIMIT));
  }

  private boolean isEmptyExtensionLimit(Object updateProductMap) {
    return StringUtils.isEmpty(((Map) updateProductMap).get(EXTENSION_LIMIT).toString().trim());
  }

  private boolean isDeleteAccessEntitlementAllow(Object accessEntitlementMap) {
    return Objects.isNull(((Map) accessEntitlementMap).get(ALLOW_ATTRIBUTE));
  }

  private boolean isDeleteAccessEntitlementDeny(Object accessEntitlementMap) {
    return Objects.isNull(((Map) accessEntitlementMap).get(DENY_ATTRIBUTE));
  }

  private boolean hasExtensionLimit(Object updateProductMap) {
    return ((Map) updateProductMap).containsKey(EXTENSION_LIMIT);
  }

  private boolean hasAccessEntitlementAllow(Object accessEntitlementMap) {
    return ((Map) accessEntitlementMap).containsKey(ALLOW_ATTRIBUTE);
  }

  private boolean hasAccessEntitlementDeny(Object accessEntitlementMap) {
    return ((Map) accessEntitlementMap).containsKey(DENY_ATTRIBUTE);
  }

  /**
   * Checks whether valid data entitlement exist in incoming payload
   *
   * @param updateProductMap incoming product payload
   * @return true or false
   */
  private boolean isDataEntitlementExist(Map<String, Object> updateProductMap) {
    return (updateProductMap.containsKey(ENTITLEMENTS)
        && updateProductMap.get(ENTITLEMENTS) instanceof Map
        && ((Map) updateProductMap.get(ENTITLEMENTS)).containsKey(DATA_ATTRIBUTE));
  }

  /**
   * checks whether existing product is approved or deprecated
   *
   * @param existingProductDTO existing product
   * @return true or false
   */
  private boolean isProductApprovedOrDeprecated(ProductDTO existingProductDTO) {
    return existingProductDTO.getStatus().equals(ProductStatusType.approved)
        || existingProductDTO.getStatus().equals(ProductStatusType.deprecated);
  }

  /**
   * checks whether updated product is approved or deprecated
   *
   * @param updateProductMap updated product map
   * @return true or false
   */
  private boolean isUpdatedProductApprovedOrDeprecated(Map<String, Object> updateProductMap) {
    return updateProductMap
            .get(STATUS)
            .toString()
            .equalsIgnoreCase(ProductStatusType.approved.getDescription())
        || updateProductMap
            .get(STATUS)
            .toString()
            .equalsIgnoreCase(ProductStatusType.deprecated.getDescription());
  }

  /**
   * checks whether user can update the product to deprecated or not
   *
   * @param existingProductDTO existing product
   * @param updateProductMap updated product map
   * @return true or false
   */
  private boolean canUpdateDeprecated(
      ProductDTO existingProductDTO, Map<String, Object> updateProductMap) {
    return (CommonUtil.hasInternalAdminToken()
            && !CommonUtil.isBizDevUser()
            && !updateProductMap
                .get(STATUS)
                .toString()
                .equalsIgnoreCase(existingProductDTO.getStatus().name()))
        && updateProductMap
            .get(STATUS)
            .toString()
            .equalsIgnoreCase(ProductStatusType.deprecated.getDescription());
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

  /**
   * performs validation on credential schema required attributes
   *
   * @param updateProductMap Product details
   * @param errors Errors
   */
  private void validateCredential(Map<String, Object> updateProductMap, Errors errors) {

    if (updateProductMap.containsKey(CREDENTIAL)
        && Objects.nonNull(updateProductMap.get(CREDENTIAL))) {
      List<CredentialDTO> credentials = new ArrayList<>();
      try {
        credentials =
            objectMapper.convertValue(
                updateProductMap.get(CREDENTIAL), new TypeReference<List<CredentialDTO>>() {});
      } catch (IllegalArgumentException ex) {

        throw new EpcRuntimeException(
            HttpStatus.BAD_REQUEST,
            messageUtil.getMessage(
                "synkrato.services.partner.product.invalid.content.error",
                new Object[] {CREDENTIAL}));
      }

      credentials.forEach(
          credentialDTO -> {
            if (StringUtils.isEmpty(credentialDTO.getId())) {
              errors.rejectValue(
                  CREDENTIAL,
                  "synkrato.services.partner.product.attributes.required",
                  new Object[] {ID},
                  EMPTY_STRING);
            }
            if (StringUtils.isEmpty(credentialDTO.getTitle())) {
              errors.rejectValue(
                  CREDENTIAL,
                  "synkrato.services.partner.product.attributes.required",
                  new Object[] {CREDENTIAL_TYPE_TITLE},
                  EMPTY_STRING);
            }
            if (Objects.isNull(credentialDTO.getType())) {
              errors.rejectValue(
                  CREDENTIAL,
                  "synkrato.services.partner.product.attributes.required",
                  new Object[] {CREDENTIAL_TYPE_KEY},
                  EMPTY_STRING);
            }
          });

      if (productUtil.containDuplicateCredential(credentials)) {
        errors.rejectValue(ID, "synkrato.services.partner.product.credentials.duplicate");
      }
    }
  }

  /**
   * Gets list of requestTypes removed from product definition
   *
   * @param updateProductMap incoming product payload
   * @param existingProductDTO existing product payload
   * @return list of requestTypes removed from product definition
   */
  private List<String> getRemovedRequestTypes(
      Map<String, Object> updateProductMap, ProductDTO existingProductDTO) {
    List<String> removedRequestTypes = new ArrayList<>();

    if (Objects.nonNull(updateProductMap.get(REQUEST_TYPES_ATTRIBUTE))
        && updateProductMap.get(REQUEST_TYPES_ATTRIBUTE) instanceof List) {
      removedRequestTypes.addAll(existingProductDTO.getRequestTypes());
      removedRequestTypes.removeAll((List) updateProductMap.get(REQUEST_TYPES_ATTRIBUTE));
    }
    return removedRequestTypes;
  }

  /**
   * performs validation on data entitlements request types
   *
   * @param updateProductMap Product details
   * @param errors Errors
   */
  private void validateDataEntitlements(
      Map<String, Object> updateProductMap, ProductDTO existingProductDTO, Errors errors) {
    log.debug("validate_data_entitlements {}", LOGGER_START);

    if (updateProductMap.containsKey(REQUEST_TYPES_ATTRIBUTE)
        && !isDataEntitlementExist(updateProductMap)) {

      List<String> removedRequestTypes =
          getRemovedRequestTypes(updateProductMap, existingProductDTO);

      if (Objects.nonNull(existingProductDTO.getEntitlements())
          && Objects.nonNull(existingProductDTO.getEntitlements().getData())
          && Objects.nonNull(existingProductDTO.getEntitlements().getData().getTransactions())) {
        List<String> requestTypesInUse = new ArrayList<>();

        for (TransactionEntitlementDTO transactionEntitlementDTO :
            existingProductDTO.getEntitlements().getData().getTransactions()) {
          requestTypesInUse.addAll(
              getRequestTypesInUse(
                  transactionEntitlementDTO.getRequestTypes(), removedRequestTypes));
        }

        if (!CollectionUtils.isEmpty(requestTypesInUse)) {
          errors.rejectValue(
              DATA_ENTITLEMENTS,
              REQUEST_TYPE_ATTRIBUTE_DELETE_ERROR,
              new Object[] {requestTypesInUse, TRANSACTIONS},
              EMPTY_STRING);
        }
      }
    }

    validateResources(updateProductMap, errors);

    log.debug("validate_data_entitlements {}", LOGGER_END);
  }

  /**
   * Validate $.entitlements.data.transactions[*].response.resources[*]
   *
   * @param updateProductMap
   * @param errors
   */
  private void validateResources(Map<String, Object> updateProductMap, Errors errors) {
    log.debug("validate_resources {}", LOGGER_START); // NOSONAR

    List<Map<String, Object>> transactions =
        JsonPath.using(JSON_PATH_CONFIGURATION).parse(updateProductMap).read(TRANSACTIONS_JSONPATH);

    if (CollectionUtils.isEmpty(transactions)) {
      return;
    }

    for (int txnIndex = 0; txnIndex < transactions.size(); txnIndex++) {

      List<String> resources =
          (List<String>)
              ((Map<String, Object>)
                      transactions.get(txnIndex).getOrDefault(RESPONSE, new HashMap<>()))
                  .getOrDefault(RESOURCES, new ArrayList<>());

      validateResources(txnIndex, Optional.ofNullable(resources).orElse(new ArrayList<>()), errors);
    }

    log.debug("validate_resources {}", LOGGER_END);
  }

  /**
   * Validate list of resources.
   *
   * @param txnIndex
   * @param resources
   * @param errors
   */
  private void validateResources(int txnIndex, List<String> resources, Errors errors) {
    log.debug("validate_resources {}", LOGGER_START);

    for (int resourceIndex = 0; resourceIndex < resources.size(); resourceIndex++) {
      String resource = resources.get(resourceIndex);

      if (!ObjectUtils.isEmpty(resource) && resource.length() > MAX_LENGTH) {

        errors.rejectValue(
            String.format(
                "entitlements.data.transactions[%s].response.resources[%s]",
                txnIndex, resourceIndex),
            "synkrato.services.partner.product.attribute.invalid.size",
            new Object[] {MAX_LENGTH},
            "");
      }
    }

    log.debug("validate_resources {}", LOGGER_END);
  }

  /**
   * performs validation on options schema required attributes
   *
   * @param updateProductMap Product details
   * @param errors Errors
   */
  private void validateOptions(
      Map<String, Object> updateProductMap, ProductDTO existingProductDTO, Errors errors) {
    log.debug("validate_options {}", LOGGER_START);

    List<String> removedRequestTypes = getRemovedRequestTypes(updateProductMap, existingProductDTO);
    if (updateProductMap.containsKey(OPTIONS) && Objects.nonNull(updateProductMap.get(OPTIONS))) {
      List<OptionsDTO> options;
      try {
        options =
            objectMapper.convertValue(
                updateProductMap.get(OPTIONS), new TypeReference<List<OptionsDTO>>() {});
      } catch (IllegalArgumentException ex) {
        log.warn("Invalid options schema", ex);
        throw new EpcRuntimeException(
            HttpStatus.BAD_REQUEST,
            messageUtil.getMessage(
                "synkrato.services.partner.product.invalid.content.error", new Object[] {OPTIONS}));
      }

      validateOptionsRequiredFields(options, removedRequestTypes, errors);

    } else if (!CollectionUtils.isEmpty(existingProductDTO.getOptions())) {
      List<String> requestTypesInUse = new ArrayList<>();

      for (OptionsDTO optionsDTO : existingProductDTO.getOptions()) {
        requestTypesInUse.addAll(
            getRequestTypesInUse(optionsDTO.getRequestTypes(), removedRequestTypes));
      }

      if (!CollectionUtils.isEmpty(requestTypesInUse)) {
        errors.rejectValue(
            OPTIONS,
            REQUEST_TYPE_ATTRIBUTE_DELETE_ERROR,
            new Object[] {requestTypesInUse, OPTIONS},
            EMPTY_STRING);
      }
    }

    log.debug("validate_options {}", LOGGER_END);
  }

  /**
   * validates options required fields
   *
   * @param options incoming options payload
   * @param removedRequestTypes removed requestTypes
   * @param errors list of errors
   */
  private void validateOptionsRequiredFields(
      List<OptionsDTO> options, List<String> removedRequestTypes, Errors errors) {
    log.debug("validate_options_required_fields {}", LOGGER_START);

    List<String> optionsRequestTypesInUse = new ArrayList<>();

    for (OptionsDTO optionsDTO : options) {
      if (CollectionUtils.isEmpty(optionsDTO.getRequestTypes())) {
        errors.rejectValue(
            OPTIONS,
            "synkrato.services.epc.common.field.param.not-empty",
            new Object[] {REQUEST_TYPES_ATTRIBUTE},
            EMPTY_STRING);
      } else {
        optionsRequestTypesInUse.addAll(
            getRequestTypesInUse(optionsDTO.getRequestTypes(), removedRequestTypes));

        if (Objects.isNull(optionsDTO.getSchema())) {
          errors.rejectValue(
              OPTIONS,
              "synkrato.services.partner.product.attributes.required",
              new Object[] {SCHEMA},
              EMPTY_STRING);
        }
      }
    }

    if (!CollectionUtils.isEmpty(optionsRequestTypesInUse)) {
      errors.rejectValue(
          OPTIONS,
          REQUEST_TYPE_ATTRIBUTE_DELETE_ERROR,
          new Object[] {optionsRequestTypesInUse, OPTIONS},
          EMPTY_STRING);
    }

    log.debug("validate_options_required_fields {}", LOGGER_END);
  }

  /**
   * Finds whether the options requestTypes are removed from product level
   *
   * @param requestTypesInUse existing product request types from data entitlements or options
   * @param removedRequestTypes removed request types from payload
   * @return list of request types
   */
  private List<String> getRequestTypesInUse(
      List<String> requestTypesInUse, List<String> removedRequestTypes) {
    log.debug("get_request_types_in_use {}", LOGGER_START);

    List<String> inUseRequestTypes = new ArrayList<>(removedRequestTypes);
    inUseRequestTypes.retainAll(requestTypesInUse);

    log.debug("get_request_types_in_use {}", LOGGER_END);
    return inUseRequestTypes;
  }
}
