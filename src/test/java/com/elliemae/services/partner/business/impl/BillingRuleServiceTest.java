package com.synkrato.services.partner.business.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.synkrato.components.microservice.exception.DataNotFoundException;
import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.enums.BillingRuleStatus;
import com.synkrato.services.epc.common.dto.enums.ProductStatusType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.data.BillingRule;
import com.synkrato.services.partner.data.Product;
import com.synkrato.services.partner.data.jpa.BillingRuleRepository;
import com.synkrato.services.partner.data.jpa.ProductRepository;
import com.synkrato.services.partner.enums.EntityView;
import com.synkrato.services.partner.util.BillingRuleUpdateUtil;
import com.synkrato.services.partner.util.BillingRuleUtil;
import com.synkrato.services.partner.util.ProductUtil;
import com.synkrato.services.partner.util.TestHelper;
import com.synkrato.services.partner.validators.BillingRuleUpdateValidator;
import com.synkrato.services.partner.validators.BillingRuleValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RunWith(MockitoJUnitRunner.class)
public class BillingRuleServiceTest {

  @Mock BillingRuleRepository billingRuleRepository;
  @Mock ProductRepository productRepository;
  @Mock BillingRuleUtil billingRuleUtil;
  @Mock BillingRuleUpdateUtil billingRuleUpdateUtil;
  @Mock BillingRuleValidator billingRuleValidator;
  @Mock BillingRuleUpdateValidator billingRuleUpdateValidator;
  @Mock ProductUtil productUtil;

  @Mock MessageUtil messageUtil;

  @InjectMocks BillingRuleServiceImpl billingRuleService;
  @Mock BillingRuleChangeLogServiceImpl billingRuleChangeLogService;
  @Mock ProductServiceImpl productService;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    ObjectMapper objectMapper = new ObjectMapper();
    ReflectionTestUtils.setField(billingRuleService, "objectMapper", objectMapper);
    ReflectionTestUtils.setField(billingRuleService, "messageUtil", messageUtil);
    ReflectionTestUtils.setField(
        billingRuleService, "billingRuleUpdateUtil", billingRuleUpdateUtil);
    ReflectionTestUtils.setField(
        billingRuleService, "billingRuleUpdateValidator", billingRuleUpdateValidator);
  }

  /**
   * This is a positive test case where the product exists and there is no existing billing rule in
   * draft state for the given product
   */
  @Test
  public void createbillingRuleValid() {

    // arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.setProductId(product.getId().toString());
    BillingRule billingRule = TestHelper.buildBillingRule();
    String view = "default";

    // mock
    when(billingRuleUtil.buildBillingRule(billingRuleDTO)).thenReturn(billingRule);
    when(billingRuleRepository.saveAndFlush(billingRule)).thenReturn(billingRule);
    when(billingRuleUtil.buildBillingRuleDTO(billingRule)).thenReturn(billingRuleDTO);
    when(productService.findById(billingRuleDTO.getProductId(), EntityView.ID.name()))
        .thenReturn(productDTO);

    // act
    BillingRuleDTO billingRuleResult = billingRuleService.create(billingRuleDTO);

    // assert
    assertEquals(billingRuleDTO, billingRuleResult);
  }

  /**
   * This is a negative test case where for the given productId there is a billing rule which exists
   * in DRAFT state.
   */
  @Test
  public void createbillingRuleValidWithExistingDraft() {

    // arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.setProductId(product.getId().toString());
    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setProductId(product.getId());
    List<BillingRule> billingRuleList = new ArrayList<>();
    billingRuleList.add(billingRule);
    billingRuleDTO.setStatus(BillingRuleStatus.DRAFT);
    List<BillingRuleDTO> billingRuleDTOList = new ArrayList<>();
    billingRuleDTOList.add(billingRuleDTO);
    String view = "default";
    boolean result = false;

    // mock
    when(billingRuleUtil.buildBillingRulesDTO(billingRuleList)).thenReturn(billingRuleDTOList);
    when(billingRuleRepository.findByProductId(product.getId())).thenReturn(billingRuleList);
    when(productService.findById(billingRuleDTO.getProductId(), EntityView.ID.name()))
        .thenReturn(productDTO);

    // act
    try {
      BillingRuleDTO billingRuleResult = billingRuleService.create(billingRuleDTO);

    } catch (EpcRuntimeException ex) {

      if (HttpStatus.CONFLICT.equals(ex.getHttpStatus())) {
        result = true;
      }
    }

    // assert
    assertTrue(result);
  }

  /** This is a negative test case where the given productId is deprecated */
  @Test
  public void createbillingRuleValidWithDeprecatedProduct() {

    // arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setStatus(ProductStatusType.deprecated);
    Product product = TestHelper.buildProduct(productDTO);
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.setProductId(product.getId().toString());
    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setProductId(product.getId());
    List<BillingRule> billingRuleList = new ArrayList<>();
    billingRuleList.add(billingRule);
    billingRuleDTO.setStatus(BillingRuleStatus.DRAFT);
    List<BillingRuleDTO> billingRuleDTOList = new ArrayList<>();
    billingRuleDTOList.add(billingRuleDTO);
    String view = "default";
    boolean result = false;
    EpcRuntimeException epcRuntimeException =
        new EpcRuntimeException(
            HttpStatus.FORBIDDEN,
            messageUtil.getMessage("synkrato.services.partner.product.billing.status.deprecated"));
    HttpStatus httpStatus = null;

    // mock
    when(productService.findById(billingRuleDTO.getProductId(), EntityView.ID.name()))
        .thenReturn(productDTO);
    doThrow(epcRuntimeException)
        .when(billingRuleValidator)
        .validateBillingRuleForProduct(productDTO, billingRuleDTO);

    // act
    try {
      BillingRuleDTO billingRuleResult = billingRuleService.create(billingRuleDTO);

    } catch (EpcRuntimeException ex) {
      httpStatus = ex.getHttpStatus();
    }

    // assert
    assertEquals(HttpStatus.FORBIDDEN, httpStatus);
  }

  /**
   * This is a negative test case where the given requestTypes defined in the transformations are
   * not present in the product definition
   */
  @Test
  public void createbillingRuleValidWithInvalidRequestTypes() {

    // arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO
        .getTransformations()
        .get(0)
        .getTransaction()
        .put("requestType", "Asset Verification");
    billingRuleDTO.setProductId(product.getId().toString());
    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setProductId(product.getId());
    List<BillingRule> billingRuleList = new ArrayList<>();
    billingRuleList.add(billingRule);
    billingRuleDTO.setStatus(BillingRuleStatus.DRAFT);
    List<BillingRuleDTO> billingRuleDTOList = new ArrayList<>();
    billingRuleDTOList.add(billingRuleDTO);
    String view = "default";
    boolean result = false;
    EpcRuntimeException epcRuntimeException =
        new EpcRuntimeException(
            HttpStatus.BAD_REQUEST,
            messageUtil.getMessage(
                "synkrato.services.partner.product.billing.transformations.requestType.not-found"));
    HttpStatus httpStatus = null;

    // mock
    when(productService.findById(billingRuleDTO.getProductId(), EntityView.ID.name()))
        .thenReturn(productDTO);
    doThrow(epcRuntimeException)
        .when(billingRuleValidator)
        .validateBillingRuleForProduct(productDTO, billingRuleDTO);

    // act
    try {
      BillingRuleDTO billingRuleResult = billingRuleService.create(billingRuleDTO);

    } catch (EpcRuntimeException ex) {
      httpStatus = ex.getHttpStatus();
    }

    // assert
    assertEquals(HttpStatus.BAD_REQUEST, httpStatus);
  }

  /** This is a negative test case where we are creating a new billing rule in Approved state */
  @Test
  public void createbillingRuleValidWithApprovedStatus() {

    // arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.setProductId(product.getId().toString());
    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setProductId(product.getId());
    billingRuleDTO.setStatus(BillingRuleStatus.APPROVED);

    String view = "default";
    boolean result = false;
    HttpStatus httpStatus = null;

    // mock
    when(productService.findById(billingRuleDTO.getProductId(), EntityView.ID.name()))
        .thenReturn(productDTO);

    // act
    try {
      BillingRuleDTO billingRuleResult = billingRuleService.create(billingRuleDTO);

    } catch (EpcRuntimeException ex) {
      httpStatus = ex.getHttpStatus();
    }

    // assert
    assertEquals(HttpStatus.BAD_REQUEST, httpStatus);
  }

  /**
   * This is a negative test case where the product does not exist and a billing rule is being
   * created against it
   */
  @Test
  public void createbillingRuleWithInvalidProductId() {

    // arrange
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.setProductId(UUID.randomUUID().toString());

    String view = "default";
    boolean result = false;

    // mock
    when(productService.findById(anyString(), anyString()))
        .thenThrow(new DataNotFoundException("Product", "id"));

    // act
    try {
      BillingRuleDTO billingRuleResult = billingRuleService.create(billingRuleDTO);

    } catch (DataNotFoundException ex) {

      if (HttpStatus.NOT_FOUND.equals(ex.getHttpStatus())) {
        result = true;
      }
    }

    // assert
    assertTrue(result);
  }

  /** This is a negative test case where the user is not authorized */
  @Test
  public void createbillingRuleValidWithUnauthorizedAccess() {

    // arrange
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.setProductId(UUID.randomUUID().toString());
    String view = "default";
    boolean result = false;

    // mock
    when(productService.findById(anyString(), anyString()))
        .thenThrow(new EpcRuntimeException(HttpStatus.FORBIDDEN, ""));

    // act
    try {
      BillingRuleDTO billingRuleResult = billingRuleService.create(billingRuleDTO);

    } catch (EpcRuntimeException ex) {

      if (HttpStatus.FORBIDDEN.equals(ex.getHttpStatus())) {
        result = true;
      }
    }

    // assert
    assertTrue(result);
  }

  /**
   * This method will test for successful update of billingRule
   *
   * @throws MethodArgumentNotValidException
   */
  @Test
  public void updateBillingRuleWithDraftSuccess() throws MethodArgumentNotValidException {

    // arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    productDTO.setId(product.getId().toString());

    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    UUID billingRuleID = UUID.randomUUID();
    billingRuleDTO.setId(billingRuleID.toString());

    BillingRuleDTO mergedBillingRule = BillingRuleDTO.builder().build();
    BeanUtils.copyProperties(billingRuleDTO, mergedBillingRule);

    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});
    billingRuleDTO.setProductId(product.getId().toString());

    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setId(billingRuleID);
    BillingRule updateBillingRule = BillingRule.builder().build();
    BeanUtils.copyProperties(billingRule, updateBillingRule);
    updateBillingRule.setVersionNumber(1);

    TestHelper.buildInternalAdminJwt();

    // mock

    when(billingRuleRepository.saveAndFlush(billingRule)).thenReturn(updateBillingRule);
    when(billingRuleUtil.buildBillingRuleDTO(billingRule)).thenReturn(billingRuleDTO);
    when(productService.findById(billingRuleDTO.getProductId(), EntityView.ID.name()))
        .thenReturn(productDTO);
    when(billingRuleUpdateUtil.merge(billingRuleMap, billingRuleDTO)).thenReturn(mergedBillingRule);
    when(billingRuleRepository.findOptionalByProductIdAndId(any(UUID.class), any(UUID.class)))
        .thenReturn(Optional.of(billingRule));

    // act
    BillingRuleDTO billingRuleResult =
        billingRuleService.update(productDTO.getId(), billingRuleID.toString(), billingRuleMap);

    // assert
    assertEquals(billingRuleDTO, billingRuleResult);
  }

  /**
   * This method will test for successful update of billingRule with Approved status
   *
   * @throws MethodArgumentNotValidException
   */
  @Test
  public void updateBillingRuleWithApprovedSuccess() throws MethodArgumentNotValidException {

    // arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    productDTO.setId(product.getId().toString());

    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    UUID billingRuleID = UUID.randomUUID();
    billingRuleDTO.setId(billingRuleID.toString());
    billingRuleDTO.setStatus(BillingRuleStatus.APPROVED);

    BillingRuleDTO mergedBillingRule = BillingRuleDTO.builder().build();
    BeanUtils.copyProperties(billingRuleDTO, mergedBillingRule);
    mergedBillingRule.setProductId(product.getId().toString());

    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});
    billingRuleDTO.setProductId(product.getId().toString());

    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setId(billingRuleID);
    BillingRule updateBillingRule = BillingRule.builder().build();
    BeanUtils.copyProperties(billingRule, updateBillingRule);
    updateBillingRule.setVersionNumber(1);
    updateBillingRule.setStatus(BillingRuleStatus.APPROVED.getValue());

    List<BillingRule> billingRules = TestHelper.buildBillingRulesList();

    TestHelper.buildInternalAdminJwt();

    // mock

    when(billingRuleRepository.saveAndFlush(billingRule)).thenReturn(updateBillingRule);
    when(billingRuleUtil.buildBillingRuleDTO(billingRule)).thenReturn(billingRuleDTO);
    when(productService.findById(billingRuleDTO.getProductId(), EntityView.ID.name()))
        .thenReturn(productDTO);
    when(billingRuleUpdateUtil.merge(billingRuleMap, billingRuleDTO)).thenReturn(mergedBillingRule);
    when(billingRuleRepository.findOptionalByProductIdAndId(any(UUID.class), any(UUID.class)))
        .thenReturn(Optional.of(billingRule));
    when(billingRuleRepository.findByProductId(any(UUID.class))).thenReturn(billingRules);

    // act
    BillingRuleDTO billingRuleResult =
        billingRuleService.update(productDTO.getId(), billingRuleID.toString(), billingRuleMap);

    // assert
    assertEquals(billingRuleDTO, billingRuleResult);
  }

  /**
   * This method will test for successful update of billingRule with Approved status and Partner JWT
   *
   * @throws MethodArgumentNotValidException
   */
  @Test
  public void updateBillingRuleWithApprovedFailure() throws MethodArgumentNotValidException {

    // arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    productDTO.setId(product.getId().toString());

    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    UUID billingRuleID = UUID.randomUUID();
    billingRuleDTO.setId(billingRuleID.toString());
    billingRuleDTO.setStatus(BillingRuleStatus.APPROVED);

    BillingRuleDTO mergedBillingRule = BillingRuleDTO.builder().build();
    BeanUtils.copyProperties(billingRuleDTO, mergedBillingRule);
    mergedBillingRule.setProductId(product.getId().toString());

    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});
    billingRuleDTO.setProductId(product.getId().toString());

    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setId(billingRuleID);
    BillingRule updateBillingRule = BillingRule.builder().build();
    BeanUtils.copyProperties(billingRule, updateBillingRule);
    updateBillingRule.setVersionNumber(1);
    updateBillingRule.setStatus(BillingRuleStatus.APPROVED.getValue());

    List<BillingRule> billingRules = TestHelper.buildBillingRulesList();

    HttpStatus httpStatus = null;

    TestHelper.buildPartnerJWT();

    // mock

    when(productService.findById(billingRuleDTO.getProductId(), EntityView.ID.name()))
        .thenReturn(productDTO);

    EpcRuntimeException epcRuntimeException =
        new EpcRuntimeException(
            HttpStatus.FORBIDDEN, messageUtil.getMessage("synkrato.services.epc.forbidden.error"));

    doThrow(epcRuntimeException)
        .when(billingRuleUpdateValidator)
        .validateIfAuthorizedToUpdate(productDTO);

    try {
      // act
      BillingRuleDTO billingRuleResult =
          billingRuleService.update(productDTO.getId(), billingRuleID.toString(), billingRuleMap);

    } catch (EpcRuntimeException ex) {
      httpStatus = ex.getHttpStatus();
    }

    assertEquals(HttpStatus.FORBIDDEN, httpStatus);
  }

  /**
   * This method will test for billingRule with a status that is not DRAFT
   *
   * @throws MethodArgumentNotValidException
   */
  @Test
  public void updateBillingRuleWithInvalidBillingRuleStatus()
      throws MethodArgumentNotValidException {

    // arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    UUID billingRuleID = UUID.randomUUID();
    Product product = TestHelper.buildProduct(productDTO);
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();

    productDTO.setId(product.getId().toString());
    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});
    billingRuleDTO.setProductId(product.getId().toString());
    billingRuleDTO.setStatus(BillingRuleStatus.APPROVED);

    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setId(billingRuleID);
    billingRule.setStatus(BillingRuleStatus.APPROVED.getValue());
    HttpStatus httpStatus = null;

    TestHelper.buildPartnerJWT();
    String exception = "";
    String errorMessage = "A billing rule can be modified only when status is 'DRAFT'";

    // mock

    when(productService.findById(billingRuleDTO.getProductId(), EntityView.ID.name()))
        .thenReturn(productDTO);
    when(messageUtil.getMessage("synkrato.services.partner.product.billing.status.invalid.action"))
        .thenReturn(errorMessage);
    when(billingRuleRepository.findOptionalByProductIdAndId(any(UUID.class), any(UUID.class)))
        .thenReturn(Optional.of(billingRule));

    // act
    try {
      BillingRuleDTO billingRuleResult =
          billingRuleService.update(productDTO.getId(), billingRuleID.toString(), billingRuleMap);

    } catch (EpcRuntimeException ex) {
      exception = ex.getMessage();
      httpStatus = ex.getHttpStatus();
    }

    // assert
    assertEquals(errorMessage, exception);
    assertEquals(HttpStatus.BAD_REQUEST, httpStatus);
  }

  /**
   * This method will test for invalid billingRuleId case
   *
   * @throws MethodArgumentNotValidException
   */
  @Test
  public void updateBillingRuleWithInvalidBillingRuleId() throws MethodArgumentNotValidException {

    // arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    UUID billingRuleID = UUID.randomUUID();
    Product product = TestHelper.buildProduct(productDTO);
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    String errorMessage =
        "Entity 'BillingRule' with identifier '" + billingRuleID.toString() + "' not found.";
    String exception = "";
    HttpStatus httpStatus = null;

    productDTO.setId(product.getId().toString());
    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});
    billingRuleDTO.setProductId(product.getId().toString());

    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setId(billingRuleID);

    TestHelper.buildInternalAdminJwt();

    // mock

    when(productService.findById(billingRuleDTO.getProductId(), EntityView.ID.name()))
        .thenReturn(productDTO);

    // act
    try {
      BillingRuleDTO billingRuleResult =
          billingRuleService.update(productDTO.getId(), billingRuleID.toString(), billingRuleMap);

    } catch (DataNotFoundException ex) {
      exception = ex.getMessage();
      httpStatus = ex.getHttpStatus();
    }

    // assert
    assertEquals(HttpStatus.NOT_FOUND, httpStatus);
    assertEquals(errorMessage, exception);
  }
}
