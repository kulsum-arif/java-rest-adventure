package com.synkrato.services.partner.validators;

import static com.synkrato.services.epc.common.EpcCommonConstants.EMPTY_STRING;
import static com.synkrato.services.epc.common.EpcCommonConstants.ENVIRONMENT_ATTRIBUTE;
import static com.synkrato.services.epc.common.EpcCommonConstants.ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.INTEGRATION_TYPE;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;
import static com.synkrato.services.epc.common.EpcCommonConstants.PARTNER_ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.STATUS_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.DATA_ENTITLEMENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.EXTENSION_LIMIT;
import static com.synkrato.services.partner.PartnerServiceConstants.REQUEST_TYPES_ATTRIBUTE;

import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.enums.IntegrationType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.ProductService;
import com.synkrato.services.partner.util.ProductUtil;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Slf4j
@Component
public class ProductCreateValidator extends ProductValidator implements Validator {

  @Autowired private MessageUtil messageUtil;
  @Autowired private ProductService productService;
  @Autowired private ProductUtil productUtil;
  private static final String READ_ONLY_ERROR = "synkrato.services.partner.product.readonly";
  private static final String SUBSCRIPTION_ID = "subscriptionId";
  private static final String WEBHOOKS = "webhooks";

  @Override
  public boolean supports(Class<?> aClass) {
    return ProductDTO.class.equals(aClass);
  }

  @Override
  public void validate(Object product, Errors errors) {
    log.debug("Product Create Validator {}", LOGGER_START);

    ProductDTO productDTO = (ProductDTO) product;
    String productId = ProductUtil.getPartnerId(productDTO);

    if (CommonUtil.hasInternalAdminToken() && StringUtils.isEmpty(productDTO.getPartnerId())) {
      errors.rejectValue(PARTNER_ID, "synkrato.services.partner.product.partner-id.required");
    }

    if (CommonUtil.hasPartnerToken() && !CommonUtil.isPartnerAuthorized(productId)) {
      throw new EpcRuntimeException(
          HttpStatus.FORBIDDEN, messageUtil.getMessage("synkrato.services.epc.forbidden.error"));
    }

    super.validate(productDTO, errors);
    validateRequestTypes(productDTO, errors);
    validateDuplicateProduct(productId, productDTO.getName(), errors);
    validateReadOnlyAttributes(productDTO, errors);
    validateIntegrationType(productDTO, errors);
    validateDuplicateCredential(productDTO, errors);
    validateDataEntitlements(productDTO);
    log.debug("Product Create Validator {}", LOGGER_END);
  }

  /**
   * Partner cannot touch entitlements.data
   *
   * @param productDTO
   */
  private void validateDataEntitlements(ProductDTO productDTO) {
    if (CommonUtil.hasPartnerToken()
        && Objects.nonNull(productDTO.getEntitlements())
        && Objects.nonNull(productDTO.getEntitlements().getData())) {
      throw new EpcRuntimeException(
          HttpStatus.BAD_REQUEST,
          messageUtil.getMessage(READ_ONLY_ERROR, new Object[] {DATA_ENTITLEMENTS}));
    }
  }

  private void validateDuplicateCredential(ProductDTO productDTO, Errors errors) {

    if (Objects.nonNull(productDTO.getCredentials())
        && productUtil.containDuplicateCredential(productDTO.getCredentials())) {
      errors.rejectValue(ID, "synkrato.services.partner.product.credentials.duplicate");
    }
  }

  /**
   * Check duplicate product
   *
   * @param errors Errors
   */
  private void validateDuplicateProduct(String productId, String productName, Errors errors) {

    if (!StringUtils.isEmpty(productId) && !StringUtils.isEmpty(productName)) {

      List<ProductDTO> productDTOList =
          productService.findByPartnerIdAndName(productId, productName);

      if (!CollectionUtils.isEmpty(productDTOList)) {
        errors.rejectValue(
            PARTNER_ID,
            "synkrato.services.partner.product.duplicate.error",
            new Object[] {productId, productName},
            EMPTY_STRING);
      }
    }
  }

  /**
   * Validate status and environment - They are read only for create product
   *
   * @param productDTO Product details
   * @param errors Errors
   */
  private void validateReadOnlyAttributes(ProductDTO productDTO, Errors errors) {
    if (!StringUtils.isEmpty(productDTO.getStatus())) {
      errors.rejectValue(
          STATUS_ATTRIBUTE,
          "synkrato.services.partner.product.readonly",
          new Object[] {STATUS_ATTRIBUTE},
          EMPTY_STRING);
    }
    if (!StringUtils.isEmpty(productDTO.getEnvironment())) {
      errors.rejectValue(
          ENVIRONMENT_ATTRIBUTE,
          "synkrato.services.partner.product.readonly",
          new Object[] {ENVIRONMENT_ATTRIBUTE},
          EMPTY_STRING);
    }
    if (CommonUtil.hasPartnerToken() && Objects.nonNull(productDTO.getExtensionLimit())) {
      errors.rejectValue(
          EXTENSION_LIMIT,
          "synkrato.services.partner.product.readonly",
          new Object[] {EXTENSION_LIMIT},
          EMPTY_STRING);
    }

    validateWebhookSubscriptions(productDTO, errors);
  }

  /**
   * This method will validate if the product payload has any subscriptionId's
   *
   * @param productDTO Product details
   * @param errors Errors
   */
  private void validateWebhookSubscriptions(ProductDTO productDTO, Errors errors) {
    if (!CollectionUtils.isEmpty(productDTO.getWebhooks())
        && productDTO.getWebhooks().stream()
            .anyMatch(subscription -> !StringUtils.isEmpty(subscription.getSubscriptionId()))) {
      errors.rejectValue(
          WEBHOOKS,
          "synkrato.services.partner.product.readonly",
          new Object[] {SUBSCRIPTION_ID},
          EMPTY_STRING);
    }
  }

  /**
   * Validate Request Types
   *
   * @param productDTO Product Details
   * @param errors Errors
   */
  private void validateRequestTypes(ProductDTO productDTO, Errors errors) {
    if (Objects.isNull(productDTO.getRequestTypes())) {
      errors.rejectValue(
          REQUEST_TYPES_ATTRIBUTE, "synkrato.services.partner.product.request-types.required");
    }
  }

  /**
   * integrationType should only be ASYNC if it presents in partner's payload.
   *
   * @param productDTO Product Details
   * @param errors Errors
   */
  private void validateIntegrationType(ProductDTO productDTO, Errors errors) {
    if (CommonUtil.hasPartnerToken()
        && Objects.nonNull(productDTO.getIntegrationType())
        && !IntegrationType.ASYNC.equals(productDTO.getIntegrationType())) {
      errors.rejectValue(
          INTEGRATION_TYPE,
          "synkrato.services.partner.product.integrationType.readonly",
          new Object[] {INTEGRATION_TYPE},
          EMPTY_STRING);
    }
  }
}
