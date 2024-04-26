package com.synkrato.services.partner.util;

import static com.synkrato.services.epc.common.EpcCommonConstants.EMPTY_STRING;

import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.logging.LogExecutionTime;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.validators.ProductUpdateValidator;
import com.synkrato.services.partner.validators.ProductValidator;
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

@Component
@Slf4j
public class ProductUpdateUtil {

  @Autowired private MessageUtil messageUtil;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ProductValidator productValidator;
  @Autowired private ProductUpdateValidator productUpdateValidator;

  /**
   * this method merges the incoming product and existing product
   *
   * @param updateProductMap incoming product payload
   * @param productDTO existing product retrieved from db
   * @return merged product dto
   */
  @LogExecutionTime
  public ProductDTO merge(Map<String, Object> updateProductMap, ProductDTO productDTO) {
    try {
      ObjectReader objectReader = objectMapper.readerForUpdating(productDTO);
      productDTO = objectReader.readValue(objectMapper.writeValueAsString(updateProductMap));

    } catch (IOException ex) {
      log.error("merge failure {}", ex.getMessage(), ex);

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
    return productDTO;
  }

  /**
   * Perform validation after merging - validation between db entity vs merged dto
   *
   * @param mergedProductDTO merged product dto
   */
  public void postmergeValidate(ProductDTO mergedProductDTO)
      throws MethodArgumentNotValidException {

    BindingResult bindingResults = new BindException(mergedProductDTO, "ProductDTO");
    ValidationUtils.invokeValidator(productValidator, mergedProductDTO, bindingResults);
    if (bindingResults.hasErrors()) {
      MethodParameter methodParameter =
          new MethodParameter(new Object() {}.getClass().getEnclosingMethod(), 0);
      productValidator.handleErrors(methodParameter, bindingResults);
    }
  }

  /**
   * Perform validation before merging - validation between db entity vs merged dto
   *
   * @param existingProductDTO existing product dto
   */
  public void premergeValidate(Map<String, Object> updateProductMap, ProductDTO existingProductDTO)
      throws MethodArgumentNotValidException {
    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", updateProductMap);
    premergeMap.put("existingProductDTO", existingProductDTO);

    BindingResult bindingResults = new BindException(existingProductDTO, "ProductDTO");
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, bindingResults);
    if (bindingResults.hasErrors()) {
      MethodParameter methodParameter =
          new MethodParameter(new Object() {}.getClass().getEnclosingMethod(), 0);
      productUpdateValidator.handleErrors(methodParameter, bindingResults);
    }
  }
}
