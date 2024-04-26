package com.synkrato.services.partner.validators;

import static com.synkrato.services.epc.common.EpcCommonConstants.EMPTY_STRING;
import static com.synkrato.services.epc.common.EpcCommonConstants.ENVIRONMENT;
import static com.synkrato.services.epc.common.EpcCommonConstants.ENVIRONMENT_ATTRIBUTE;
import static com.synkrato.services.epc.common.EpcCommonConstants.LIMIT;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;
import static com.synkrato.services.epc.common.EpcCommonConstants.NAME_ATTRIBUTE;
import static com.synkrato.services.epc.common.EpcCommonConstants.PARTNER_ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.PARTNER_TEST_ENVIRONMENT;
import static com.synkrato.services.epc.common.EpcCommonConstants.START;

import com.synkrato.components.microservice.identity.IdentityContext;
import com.synkrato.services.epc.common.dto.enums.EnvironmentType;
import com.synkrato.services.epc.common.dto.enums.ProductStatusType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Slf4j
@Component
public class ProductSearchValidator implements Validator {

  @Value("${synkrato.epc.product.max-query-limit}")
  private int maxQueryLimit;

  @Autowired private MessageUtil messageUtil;

  @Override
  public boolean supports(Class<?> aClass) {
    return ProductSearchDTO.class.equals(aClass);
  }

  @Override
  public void validate(Object productSearchObject, Errors errors) {

    log.debug("Product Search Validator {}", LOGGER_START);

    ProductSearchDTO productSearch = (ProductSearchDTO) productSearchObject;

    validatePartnerId(productSearch);

    validatePaginationAttributes(productSearch, errors);

    validateProductEnvironment(productSearch, errors);

    validateSearchParameters(productSearch, errors);

    validateStatus(productSearch);

    log.debug("Product Search Validator {}", LOGGER_END);
  }

  /** Validate status */
  private void validateStatus(ProductSearchDTO productSearch) {

    if (Objects.nonNull(productSearch.getStatus())) {
      ProductStatusType.findByValue(productSearch.getStatus());
    }
  }

  /**
   * If Partner id is present in the search request, validate it for partner token
   *
   * @param productSearch Input search attributes
   */
  private void validatePartnerId(ProductSearchDTO productSearch) {

    if (CommonUtil.hasPartnerToken()
        && !StringUtils.isEmpty(productSearch.getPartnerId())
        && !productSearch.getPartnerId().equals(IdentityContext.get().getClientId())) {
      throw new EpcRuntimeException(
          HttpStatus.UNAUTHORIZED,
          messageUtil.getMessage("synkrato.services.partner.product.unauthorized"));
    }
  }

  /**
   * Validate pagination attributes - start and limit
   *
   * @param productSearch SearchAttribute details
   * @param errors Errors
   */
  private void validatePaginationAttributes(ProductSearchDTO productSearch, Errors errors) {

    log.debug("validate_pagination_attributes {}", LOGGER_START);

    if (!StringUtils.isEmpty(productSearch.getStart())
        && !CommonUtil.isValidInteger(productSearch.getStart())) {
      errors.rejectValue(START, "synkrato.services.epc.common.search-attribute.start.invalid");
    }

    if (!StringUtils.isEmpty(productSearch.getLimit())
        && !CommonUtil.isValidInteger(productSearch.getLimit())) { // Limit is not a valid integer
      errors.rejectValue(
          LIMIT,
          "synkrato.services.epc.common.search-attribute.limit.invalid",
          new Object[] {maxQueryLimit},
          EMPTY_STRING);
    }

    if (!StringUtils.isEmpty(productSearch.getLimit())
        && CommonUtil.isValidInteger(productSearch.getLimit())
        && (Integer.parseInt(productSearch.getLimit()) < 0
            || Integer.parseInt(productSearch.getLimit())
                > maxQueryLimit)) { // Valid integer but limit not in range
      errors.rejectValue(
          LIMIT,
          "synkrato.services.epc.common.search-attribute.limit.invalid",
          new Object[] {maxQueryLimit},
          EMPTY_STRING);
    }
  }

  /**
   * If searching by tag, validate to ensure environment is present
   *
   * @param productSearch Input search attributes
   * @param errors Error Object
   */
  private void validateProductEnvironment(ProductSearchDTO productSearch, Errors errors) {

    String environment = CommonUtil.getProductEnvironment();
    if (!StringUtils.isEmpty(environment)) {
      boolean isParterEnvironmentValidFlag =
          Stream.of(EnvironmentType.values())
              .anyMatch(environmentType -> environmentType.name().equalsIgnoreCase(environment));

      if (!isParterEnvironmentValidFlag) {
        isParterEnvironmentValidFlag = PARTNER_TEST_ENVIRONMENT.equalsIgnoreCase(environment);
      }

      if (!isParterEnvironmentValidFlag) {
        errors.rejectValue(
            ENVIRONMENT_ATTRIBUTE,
            "synkrato.services.partner.product.environment.invalid",
            new Object[] {environment},
            EMPTY_STRING);
      }
    }

    if (CommonUtil.hasPartnerToken() && StringUtils.isEmpty(environment)) {
      errors.rejectValue(
          ENVIRONMENT_ATTRIBUTE, "synkrato.services.partner.product.environment.required");
    }

    if (!CollectionUtils.isEmpty(productSearch.getTags())
        && StringUtils.isEmpty(CommonUtil.getProductEnvironment())) {
      errors.rejectValue(ENVIRONMENT, "synkrato.services.partner.product.environment.required");
    }
    if (CommonUtil.hasPartnerToken()
        && !StringUtils.isEmpty(productSearch.getPartnerId())
        && !productSearch.getPartnerId().equals(IdentityContext.get().getClientId())) {
      throw new EpcRuntimeException(
          HttpStatus.UNAUTHORIZED,
          messageUtil.getMessage(
              "synkrato.services.partner.product.unauthorized", new Object[] {PARTNER_ID}));
    }
  }

  /**
   * Validate required search parameters
   *
   * @param productSearch Input search attributes
   * @param errors Error object
   */
  private void validateSearchParameters(ProductSearchDTO productSearch, Errors errors) {

    if (!CommonUtil.hasPartnerToken()
        && StringUtils.isEmpty(productSearch.getPartnerId())
        && StringUtils.isEmpty(productSearch.getName())
        && StringUtils.isEmpty(CommonUtil.getProductEnvironment())
        && (CollectionUtils.isEmpty(productSearch.getTags()))) {
      errors.rejectValue(
          NAME_ATTRIBUTE, "synkrato.services.partner.product.search.parameters.required");
    }
  }
}
