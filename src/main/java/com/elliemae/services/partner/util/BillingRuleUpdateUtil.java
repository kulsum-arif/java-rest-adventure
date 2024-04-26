package com.synkrato.services.partner.util;

import static com.synkrato.services.epc.common.EpcCommonConstants.EMPTY_STRING;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;

import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.validators.BillingRuleUpdateValidator;
import com.synkrato.services.partner.validators.BillingRuleValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;

@SuppressWarnings({"squid:S1193", "squid:S2133"})
@Component
@Slf4j
public class BillingRuleUpdateUtil {
  @Autowired private MessageUtil messageUtil;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private BillingRuleValidator billingRuleValidator;
  @Autowired private BillingRuleUpdateValidator billingRuleUpdateValidator;

  /**
   * This method will merge the incoming billing rule
   *
   * @param updateBillingRuleMap
   * @param billingRuleDTO
   * @return merged billing rule DTO
   */
  public BillingRuleDTO merge(
      Map<String, Object> updateBillingRuleMap, BillingRuleDTO billingRuleDTO) {
    BillingRuleDTO mergedBillingRuleDTO = null;
    try {
      ObjectReader objectReader = objectMapper.readerForUpdating(billingRuleDTO);
      mergedBillingRuleDTO =
          objectReader.readValue(objectMapper.writeValueAsString(updateBillingRuleMap));

    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);

      throw new EpcRuntimeException(
          HttpStatus.BAD_REQUEST,
          messageUtil.getMessage(
              "synkrato.services.partner.product.invalid.content.error",
              new Object[] {
                ex instanceof InvalidFormatException
                    ? ((InvalidFormatException) ex).getValue().toString()
                    : EMPTY_STRING
              }));
    }
    return mergedBillingRuleDTO;
  }

  /**
   * Perform validation before merging - validation between db entity vs merged dto
   *
   * @param existingBillingRuleDTO existing product dto
   */
  public void preMergeValidate(
      Map<String, Object> updateBillingRuleMap, BillingRuleDTO existingBillingRuleDTO)
      throws MethodArgumentNotValidException {
    log.debug("pre_merge_validate {}", LOGGER_START);

    Map<String, Object> preMergeMap = new HashMap<>();
    preMergeMap.put("updateBillingRuleMap", updateBillingRuleMap);
    preMergeMap.put("existingBillingRuleDTO", existingBillingRuleDTO);

    BindingResult bindingResults = new BindException(existingBillingRuleDTO, "BillingRuleDTO");
    ValidationUtils.invokeValidator(billingRuleUpdateValidator, preMergeMap, bindingResults);
    if (bindingResults.hasErrors()) {
      MethodParameter methodParameter =
          new MethodParameter(new Object() {}.getClass().getEnclosingMethod(), 0);
      billingRuleUpdateValidator.handleErrors(methodParameter, bindingResults);
    }

    log.debug("pre_merge_validate {}", LOGGER_END);
  }

  /**
   * Perform validation after merging - validation between db entity vs merged dto
   *
   * @param mergedBillingRuleDTO merged product dto
   */
  public void postMergeValidate(BillingRuleDTO mergedBillingRuleDTO)
      throws MethodArgumentNotValidException {
    log.debug("post_merge_validate {}", LOGGER_START);

    BindingResult bindingResults = new BindException(mergedBillingRuleDTO, "BillingRuleDTO");
    ValidationUtils.invokeValidator(billingRuleValidator, mergedBillingRuleDTO, bindingResults);
    if (bindingResults.hasErrors()) {
      MethodParameter methodParameter =
          new MethodParameter(new Object() {}.getClass().getEnclosingMethod(), 0);
      billingRuleValidator.handleErrors(methodParameter, bindingResults);
    }

    log.debug("post_merge_validate {}", LOGGER_END);
  }
}
