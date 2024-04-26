package com.synkrato.services.partner.validators;

import static com.synkrato.services.epc.common.EpcCommonConstants.ALT_TEXT;
import static com.synkrato.services.epc.common.EpcCommonConstants.DESCRIPTION;
import static com.synkrato.services.epc.common.EpcCommonConstants.OPTIONS;
import static com.synkrato.services.epc.common.EpcCommonConstants.STATUS;
import static com.synkrato.services.epc.common.EpcCommonConstants.URL;
import static com.synkrato.services.partner.PartnerServiceConstants.ACCESS_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.ADDITIONAL_LINKS;
import static com.synkrato.services.partner.PartnerServiceConstants.ALLOW_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.CREDENTIAL;
import static com.synkrato.services.partner.PartnerServiceConstants.CREDENTIAL_TYPE_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.CREDENTIAL_TYPE_REQUIRED;
import static com.synkrato.services.partner.PartnerServiceConstants.CREDENTIAL_TYPE_TITLE;
import static com.synkrato.services.partner.PartnerServiceConstants.DATA_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.DATA_ENTITLEMENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.DENY_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.ENTITLEMENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.EXTENSION_LIMIT;
import static com.synkrato.services.partner.PartnerServiceConstants.MANIFEST_ORIGIN_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.REQUEST_TYPES_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.SCHEMA;
import static com.synkrato.services.partner.PartnerServiceConstants.TAG_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.TRANSACTIONS_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.TYPE_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.WORKFLOWS;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import com.synkrato.services.epc.common.dto.AdditionalLinkDTO;
import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.FindingDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.enums.AdditionalLinkType;
import com.synkrato.services.epc.common.dto.enums.BillingRuleStatus;
import com.synkrato.services.epc.common.dto.enums.IntegrationType;
import com.synkrato.services.epc.common.dto.enums.ProductStatusType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.BillingRuleService;
import com.synkrato.services.partner.business.ProductService;
import com.synkrato.services.partner.util.ProductUtil;
import com.synkrato.services.partner.util.TestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProductUpdateValidatorTest {
  @Mock ProductService productService;
  @Mock MessageUtil messageUtil;
  @Mock ProductUtil productUtil;
  @Mock ProductValidator productValidator;
  @InjectMocks ProductUpdateValidator productUpdateValidator;
  @Mock BillingRuleService billingRuleService;
  @Spy ObjectMapper objectMapper;

  @Before
  public void setUp() {
    productUpdateValidator.setObjectMapper(new ObjectMapper());
    productUpdateValidator.setProductUtil(new ProductUtil());
    ReflectionTestUtils.setField(productUpdateValidator, "billingRuleService", billingRuleService);
    ReflectionTestUtils.setField(productUpdateValidator, "objectMapper", objectMapper);
  }

  /** Partner updates Readonly attribute - invalid id */
  @Test
  public void test1ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("id", "randomValue");

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("id", errors.getFieldError().getField());
  }

  /** Partner deletes Readonly attribute - id null */
  @Test
  public void test2ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("id", null);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("id", errors.getFieldError().getField());
  }

  /** Partner updates Readonly attribute - empty id */
  @Test
  public void test3ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("id", "");

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("id", errors.getFieldError().getField());
  }

  /** Partner updates Readonly attribute - name invalid */
  @Test
  public void test4ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("name", "testName");

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("name", errors.getFieldError().getField());
  }

  /** Partner deletes Readonly attribute - name null */
  @Test
  public void test5ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("name", null);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("name", errors.getFieldError().getField());
  }

  /** Partner updates Readonly attribute - name empty */
  @Test
  public void test6ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("name", "");

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("name", errors.getFieldError().getField());
  }

  /** Partner updates Readonly attribute - partnerId empty */
  @Test
  public void test7ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("partnerId", "009999");

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("partnerId", errors.getFieldError().getField());
  }

  /** Partner deletes Readonly attribute - partnerId null */
  @Test
  public void test8ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("partnerId", null);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("partnerId", errors.getFieldError().getField());
  }

  /** Partner deletes Readonly attribute - partnerId empty */
  @Test
  public void test9ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("partnerId", "");

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("partnerId", errors.getFieldError().getField());
  }

  /** Partner updates Readonly attribute - environment */
  @Test
  public void test10ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("environment", "prod");

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("environment", errors.getFieldError().getField());
  }

  /** Partner deletes Readonly attribute - environment */
  @Test
  public void test11ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("environment", null);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("environment", errors.getFieldError().getField());
  }

  /** Partner deletes Readonly attribute - listingName */
  @Test
  public void test12ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("listingName", null);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("listingName", errors.getFieldError().getField());
  }

  /** Partner updates approved product - listingName */
  @Test
  public void test13ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.put("status", "approved");
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setStatus(ProductStatusType.approved);
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("listingName", "testListingName");

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("listingName", errors.getFieldError().getField());
  }

  /** Partner updates - status approved */
  @Test
  public void test14ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("status", ProductStatusType.approved);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("status", errors.getFieldError().getField());
  }

  /** Partner updates - status approved */
  @Test
  public void test15ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    productMap.remove(EXTENSION_LIMIT);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("status", ProductStatusType.inreview);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertEquals(0, errors.getErrorCount());
  }

  /** Partner deletes status */
  @Test
  public void test16ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("status", null);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("status", errors.getFieldError().getField());
  }

  /** Partner changes status from approved to inreview */
  @Test
  public void test17ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setStatus(ProductStatusType.approved);
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("status", ProductStatusType.inreview);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("status", errors.getFieldError().getField());
  }

  /** Partner deletes requestTypes */
  @Test
  public void test18ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("requestTypes", null);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("requestTypes", errors.getFieldError().getField());
  }

  /** Partner updates requestTypes with invalid value */
  @Test
  public void test19ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    productMap.put("status", ProductStatusType.approved);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setStatus(ProductStatusType.approved);
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    List<String> reqTypes = new ArrayList<>();
    reqTypes.add("test");
    productMap.put("requestTypes", reqTypes);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("requestTypes", errors.getFieldError().getField());
  }

  /** Partner deletes integrationType */
  @Test
  public void test20ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("integrationType", null);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("integrationType", errors.getFieldError().getField());
  }

  /** Partner changes integrationType on approved product */
  @Test
  public void test21ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.remove(ENTITLEMENTS);
    productMap.put("status", ProductStatusType.approved);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setRequestTypes(Arrays.asList("SEARCH", "REFRESH"));

    existingProductDto.setStatus(ProductStatusType.approved);
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("integrationType", "P2P");

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("integrationType", errors.getFieldError().getField());
  }

  /** Partner deletes entitlements on approved product */
  @Test
  public void test22ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, null);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setStatus(ProductStatusType.approved);
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
    assertEquals("entitlements", errors.getFieldError().getField());
  }

  /** Admin deletes entitlements on approved product */
  @Test
  public void test23ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, null);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setStatus(ProductStatusType.approved);
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
    assertEquals("entitlements", errors.getFieldError().getField());
  }

  /** Partner updates Data entitlements on approved product */
  @Test
  public void test24ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, TestHelper.buildEntitlements());

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setStatus(ProductStatusType.approved);
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("entitlements.data.origin", errors.getFieldError().getField());
    assertEquals("entitlements.data.transactions", errors.getFieldErrors().get(1).getField());
  }

  /** Partner updates Data entitlements on sandbox product */
  @Test
  public void test25ValidateAllowUpdatePartnerFindingsForPartnerJwt() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    Map<String, Object> entitlementsMap = TestHelper.buildEntitlements();
    ((Map) entitlementsMap.get(DATA_ATTRIBUTE)).remove(MANIFEST_ORIGIN_TYPE);
    ((Map) entitlementsMap.get(DATA_ATTRIBUTE)).remove(TRANSACTIONS_ATTRIBUTE);
    productMap.put(ENTITLEMENTS, entitlementsMap);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    FindingDTO findingDTO = TestHelper.buildFindingtDTO();
    existingProductDto.getEntitlements().getData().setFindings(findingDTO);
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertFalse(errors.hasErrors());
  }

  /** Partner updates Access entitlements on sandbox product */
  @Test
  public void test26ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, TestHelper.buildEntitlements());
    ((Map) productMap.get(ENTITLEMENTS)).remove(DATA_ATTRIBUTE);
    ((Map) ((Map) productMap.get(ENTITLEMENTS)).get(ACCESS_ATTRIBUTE))
        .put(ALLOW_ATTRIBUTE, Arrays.asList("BE235342356"));

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertEquals(0, errors.getErrorCount());
  }

  /** Partner removes Data entitlements on approved product */
  @Test
  public void test27ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, TestHelper.buildEntitlements());
    ((Map) productMap.get(ENTITLEMENTS)).remove(DATA_ATTRIBUTE);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.approved);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals("entitlements.access", errors.getFieldError().getField());
  }

  /** Admin removes Data entitlements on approved product */
  @Test
  public void test28ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, TestHelper.buildEntitlements());
    ((Map) productMap.get(ENTITLEMENTS)).put(DATA_ATTRIBUTE, null);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.approved);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
    assertEquals("entitlements.data", errors.getFieldError().getField());
  }

  /** Admin removes Access entitlements deny on sandbox product */
  @Test
  public void test29ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, TestHelper.buildEntitlements());
    ((Map) productMap.get(ENTITLEMENTS)).remove(DATA_ATTRIBUTE);
    ((Map) ((Map) productMap.get(ENTITLEMENTS)).get(ACCESS_ATTRIBUTE)).put(DENY_ATTRIBUTE, null);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
    assertEquals("entitlements.access.deny", errors.getFieldError().getField());
  }

  /** Admin removes product extensionLimit on approved product */
  @Test
  public void test30ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(EXTENSION_LIMIT, null);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
    assertEquals(EXTENSION_LIMIT, errors.getFieldError().getField());
  }

  /** Partner removes Access entitlements allow on sandbox product */
  @Test
  public void test31ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, TestHelper.buildEntitlements());
    ((Map) productMap.get(ENTITLEMENTS)).remove(DATA_ATTRIBUTE);
    ((Map) ((Map) productMap.get(ENTITLEMENTS)).get(ACCESS_ATTRIBUTE)).put(ALLOW_ATTRIBUTE, null);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
    assertEquals("entitlements.access.allow", errors.getFieldError().getField());
  }

  /** Admin removes Access entitlements allow on sandbox product */
  @Test
  public void test32ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, TestHelper.buildEntitlements());
    ((Map) productMap.get(ENTITLEMENTS)).remove(DATA_ATTRIBUTE);
    ((Map) ((Map) productMap.get(ENTITLEMENTS)).get(ACCESS_ATTRIBUTE)).put(ALLOW_ATTRIBUTE, null);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
    assertEquals("entitlements.access.allow", errors.getFieldError().getField());
  }

  /** Admin removes Access entitlements on sandbox product */
  @Test
  public void test33ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, TestHelper.buildEntitlements());
    ((Map) productMap.get(ENTITLEMENTS)).remove(DATA_ATTRIBUTE);
    ((Map) productMap.get(ENTITLEMENTS)).put(ACCESS_ATTRIBUTE, null);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
    assertEquals("entitlements.access", errors.getFieldError().getField());
  }

  /** Partner removes Access entitlements on sandbox product */
  @Test
  public void test34ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, TestHelper.buildEntitlements());
    ((Map) productMap.get(ENTITLEMENTS)).remove(DATA_ATTRIBUTE);
    ((Map) productMap.get(ENTITLEMENTS)).put(ACCESS_ATTRIBUTE, null);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
    assertEquals("entitlements.access", errors.getFieldError().getField());
  }

  /** Partner removes tags on Approved product */
  @Test
  public void test35ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(TAG_ATTRIBUTE, null);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.approved);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals(TAG_ATTRIBUTE, errors.getFieldError().getField());
  }

  /** Partner updates tags on Approved product */
  @Test
  public void test36ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(TAG_ATTRIBUTE, TestHelper.buildTags());

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.approved);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals(TAG_ATTRIBUTE, errors.getFieldError().getField());
  }

  /** Partner removes credential on Approved product */
  @Test
  public void test37ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(CREDENTIAL, null);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.approved);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals(CREDENTIAL, errors.getFieldError().getField());
  }

  /** Admin removes credential on Approved product */
  @Test
  public void test38ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(CREDENTIAL, null);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.approved);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals(CREDENTIAL, errors.getFieldError().getField());
  }

  /** Admin updates tags on Approved product */
  @Test
  public void test39ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(TAG_ATTRIBUTE, TestHelper.buildTags());

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.approved);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertEquals(0, errors.getErrorCount());
  }

  /** Admin updates credential on Approved product */
  @Test
  public void test40ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(CREDENTIAL, TestHelper.buildCredential());

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.approved);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertEquals(0, errors.getErrorCount());
  }

  /** partner updates extensionLimit */
  @Test
  public void test41ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(EXTENSION_LIMIT, 0);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertEquals(1, errors.getErrorCount());
  }

  /** admin updates extensionLimit */
  @Test
  public void test42ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(EXTENSION_LIMIT, 0);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertEquals(0, errors.getErrorCount());
  }

  /** Admin updates entitlements with invalid payload - empty string */
  @Test
  public void test43ValidateEntitlements() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(ENTITLEMENTS, "");

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.invalid.content.error"));
    assertEquals("entitlements", errors.getFieldError().getField());
  }

  /** partner updates credential on development product */
  @Test
  public void test44ValidateCredential() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(CREDENTIAL, TestHelper.buildCredential());

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.development);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertEquals(0, errors.getErrorCount());
  }

  /** partner updates credential on development product */
  @Test
  public void test45ValidateCredential() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(CREDENTIAL, TestHelper.buildCredential());

    ((List) productMap.get(CREDENTIAL))
        .forEach(
            element ->
                ((Map<String, Object>) element)
                    .entrySet()
                    .removeIf(
                        entry ->
                            entry.getKey().equals("id")
                                || entry.getKey().equals(CREDENTIAL_TYPE_KEY)
                                || entry.getKey().equals(CREDENTIAL_TYPE_TITLE)
                                || entry.getKey().equals(CREDENTIAL_TYPE_REQUIRED)));

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.development);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertEquals(6, errors.getErrorCount());
  }

  /** Patch product should not allow empty extensionLimit */
  @Test
  public void test46ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(EXTENSION_LIMIT, "");
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.invalid.content.error"));
    assertEquals(EXTENSION_LIMIT, errors.getFieldError().getField());
  }

  /** Patch product should not allow empty whitespace extensionLimit */
  @Test
  public void test47ValidateReadonlyAttribute() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(EXTENSION_LIMIT, "  ");
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.invalid.content.error"));
    assertEquals(EXTENSION_LIMIT, errors.getFieldError().getField());
  }

  /** partner updates additionalLinks with wrong type */
  @Test(expected = EpcRuntimeException.class)
  public void test48ValidateAdditionalLinksFailed() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();

    List<Map<String, String>> additionalLinkDTOList = new ArrayList<>();

    Map<String, String> additionalLinkDTOMap = new HashMap<>();
    additionalLinkDTOMap.put(TYPE_KEY, "test");
    additionalLinkDTOList.add(additionalLinkDTOMap);
    productMap.put(ADDITIONAL_LINKS, additionalLinkDTOList);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);
  }

  /** partner updates additionalLinks with empty object */
  @Test
  public void test49ValidateAdditionalLinksFailed() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();

    List<Map<String, String>> additionalLinkDTOList = new ArrayList<>();

    Map<String, String> additionalLinkDTOMap1 = new HashMap<>();
    additionalLinkDTOList.add(additionalLinkDTOMap1);

    productMap.put(ADDITIONAL_LINKS, additionalLinkDTOList);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);
    assertEquals(3, errors.getFieldErrors().size());
  }

  /** partner update additionalLink without url and altText */
  @Test
  public void test51ValidateAdditionalLinksFailed() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();

    List<Map<String, String>> additionalLinkDTOList = new ArrayList<>();

    Map<String, String> additionalLinkDTOMap1 = new HashMap<>();
    additionalLinkDTOMap1.put(TYPE_KEY, AdditionalLinkType.PRODUCT_CONFIG_URL.getDescription());
    additionalLinkDTOList.add(additionalLinkDTOMap1);

    productMap.put(ADDITIONAL_LINKS, additionalLinkDTOList);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);
    assertEquals(2, errors.getFieldErrors().size());
  }

  /** partner update additionalLink without altText */
  @Test
  public void test52ValidateAdditionalLinksFailed() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();

    List<Map<String, String>> additionalLinkDTOList = new ArrayList<>();

    Map<String, String> additionalLinkDTOMap1 = new HashMap<>();
    additionalLinkDTOMap1.put(TYPE_KEY, AdditionalLinkType.PRODUCT_CONFIG_URL.getDescription());
    additionalLinkDTOMap1.put(URL, "url");
    additionalLinkDTOList.add(additionalLinkDTOMap1);

    productMap.put(ADDITIONAL_LINKS, additionalLinkDTOList);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);
    assertEquals(1, errors.getFieldErrors().size());
    assertEquals(
        "synkrato.services.partner.product.attributes.required",
        errors.getFieldErrors().get(0).getCode());
    assertEquals(ALT_TEXT, errors.getFieldErrors().get(0).getRejectedValue());
  }

  /** partner update additionalLink without description */
  @Test
  public void test53ValidateAdditionalLinksSuccess() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();

    List<Map<String, String>> additionalLinkDTOList = new ArrayList<>();

    Map<String, String> additionalLinkDTOMap1 = new HashMap<>();
    additionalLinkDTOMap1.put(TYPE_KEY, AdditionalLinkType.PRODUCT_CONFIG_URL.getDescription());
    additionalLinkDTOMap1.put(URL, "url");
    additionalLinkDTOMap1.put(ALT_TEXT, "altText");
    additionalLinkDTOList.add(additionalLinkDTOMap1);

    productMap.put(ADDITIONAL_LINKS, additionalLinkDTOList);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);
    assertEquals(0, errors.getFieldErrors().size());
  }

  /** partner update additionalLink with description size >128 */
  @Test
  public void test54ValidateAdditionalLinksFailed() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();

    List<Map<String, String>> additionalLinkDTOList = new ArrayList<>();

    Map<String, String> additionalLinkDTOMap1 = new HashMap<>();
    additionalLinkDTOMap1.put(TYPE_KEY, AdditionalLinkType.PRODUCT_CONFIG_URL.getDescription());
    additionalLinkDTOMap1.put(URL, "url");
    additionalLinkDTOMap1.put(ALT_TEXT, "altText");
    additionalLinkDTOMap1.put(
        DESCRIPTION,
        "size more than 128, size more than 128,size more than 128, size more than 128,size more than 128, size more than 128,size more than 128, size more than 128,size more than 128, size more than 128,size more than 128, size more than 128");
    additionalLinkDTOList.add(additionalLinkDTOMap1);

    productMap.put(ADDITIONAL_LINKS, additionalLinkDTOList);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);
    assertEquals(1, errors.getFieldErrors().size());
    assertEquals(1, errors.getFieldErrors().size());
    assertEquals(
        "synkrato.services.partner.product.attribute.invalid.size",
        errors.getFieldErrors().get(0).getCode());
    assertEquals(DESCRIPTION, errors.getFieldErrors().get(0).getRejectedValue());
  }

  /** partner update additionalLink with altText size >128 */
  @Test
  public void test55ValidateAdditionalLinksFailed() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();

    List<Map<String, String>> additionalLinkDTOList = new ArrayList<>();

    Map<String, String> additionalLinkDTOMap1 = new HashMap<>();
    additionalLinkDTOMap1.put(TYPE_KEY, AdditionalLinkType.PRODUCT_CONFIG_URL.getDescription());
    additionalLinkDTOMap1.put(URL, "url");
    additionalLinkDTOMap1.put(DESCRIPTION, "description");
    additionalLinkDTOMap1.put(
        ALT_TEXT,
        "size more than 128, size more than 128,size more than 128, size more than 128,size more than 128, size more than 128,size more than 128, size more than 128,size more than 128, size more than 128,size more than 128, size more than 128");
    additionalLinkDTOList.add(additionalLinkDTOMap1);

    productMap.put(ADDITIONAL_LINKS, additionalLinkDTOList);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);
    assertEquals(1, errors.getFieldErrors().size());
    assertEquals(1, errors.getFieldErrors().size());
    assertEquals(
        "synkrato.services.partner.product.attribute.invalid.size",
        errors.getFieldErrors().get(0).getCode());
    assertEquals(ALT_TEXT, errors.getFieldErrors().get(0).getRejectedValue());
  }

  /** partner updates additionalLinks with null value */
  @Test(expected = EpcRuntimeException.class)
  public void test56ValidateAdditionalLinksFailed() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();

    List<Map<String, String>> additionalLinkDTOList = new ArrayList<>();

    additionalLinkDTOList.add(null);
    productMap.put(ADDITIONAL_LINKS, additionalLinkDTOList);
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);
  }

  /** Patch product should not allow empty whitespace extensionLimit */
  @Test
  public void validateApprovedBillingRuleSuccess() {

    // Arrange
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(EXTENSION_LIMIT, 5);
    productMap.put(STATUS, "approved");
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    List<BillingRuleDTO> billingRules = TestHelper.buildBillingRulesDTOList();

    billingRules.get(0).setStatus(BillingRuleStatus.APPROVED);

    // Mock
    when(billingRuleService.findBillingRules(existingProductDto.getId())).thenReturn(billingRules);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertEquals(0, errors.getAllErrors().size());
  }

  /** Patch product should not allow approval if there is no billing rule in approved state */
  @Test
  public void validateApprovedBillingRuleFailure() {

    // Arrange
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(STATUS, "approved");
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    List<BillingRuleDTO> billingRules = TestHelper.buildBillingRulesDTOList();

    // Mock
    when(billingRuleService.findBillingRules(existingProductDto.getId())).thenReturn(billingRules);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.billingrule.approved.forbidden"));
  }

  /** Patch product should not allow approval if there are no billing rules */
  @Test
  public void validateApproveProductWithoutBillingRulesFailure() {

    // Arrange
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(STATUS, "approved");
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    List<BillingRuleDTO> billingRules = null;

    // Mock
    when(billingRuleService.findBillingRules(existingProductDto.getId())).thenReturn(billingRules);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.billingrule.approved.forbidden"));
  }

  /** Patch product with options schema - when requestTypes empty */
  @Test
  public void testValidateOptionsSchema1() throws IOException {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    List<Map<String, Object>> optionsSchemaList = new ArrayList<>();
    Map<String, Object> optionsSchema = new HashMap<>();

    // Request type is empty
    optionsSchema.put(REQUEST_TYPES_ATTRIBUTE, new ArrayList<>());

    optionsSchema.put(SCHEMA, TestHelper.getFileContent("options_schema.json"));
    optionsSchemaList.add(optionsSchema);
    productMap.put(OPTIONS, optionsSchemaList);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setOptions(TestHelper.buildOptionsDTOList());

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    assertEquals(1, errors.getFieldErrors().size());
    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.epc.common.field.param.not-empty"));
  }

  /** Patch product with options schema - when requestTypes null */
  @Test
  public void testValidateOptionsSchema2() throws IOException {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    List<Map<String, Object>> optionsSchemaList = new ArrayList<>();
    Map<String, Object> optionsSchema = new HashMap<>();

    // Request type is empty
    optionsSchema.put(REQUEST_TYPES_ATTRIBUTE, null);

    optionsSchema.put(SCHEMA, TestHelper.getFileContent("options_schema.json"));
    optionsSchemaList.add(optionsSchema);
    productMap.put(OPTIONS, optionsSchemaList);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setOptions(TestHelper.buildOptionsDTOList());

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    assertEquals(1, errors.getFieldErrors().size());
    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.epc.common.field.param.not-empty"));
  }

  /** Patch product with options schema - when schema is empty json object */
  @Test
  public void testValidateOptionsSchema3() throws IOException {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    List<Map<String, Object>> optionsSchemaList = new ArrayList<>();
    Map<String, Object> optionsSchema = new HashMap<>();

    // Request type is empty
    optionsSchema.put(REQUEST_TYPES_ATTRIBUTE, Arrays.asList("NEWREQUEST"));
    optionsSchema.put(SCHEMA, null);
    optionsSchemaList.add(optionsSchema);
    productMap.put(OPTIONS, optionsSchemaList);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setOptions(TestHelper.buildOptionsDTOList());

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    assertEquals(1, errors.getFieldErrors().size());
    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
  }

  /** Patch product with valid options schema */
  @Test
  public void testValidateOptionsSchema4() throws IOException {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    List<Map<String, Object>> optionsSchemaList = new ArrayList<>();
    Map<String, Object> optionsSchema = new HashMap<>();

    // Request type is empty
    optionsSchema.put(REQUEST_TYPES_ATTRIBUTE, Arrays.asList("NEWREQUEST"));
    optionsSchema.put(SCHEMA, TestHelper.getFileContent("options_schema.json"));
    optionsSchemaList.add(optionsSchema);
    productMap.put(OPTIONS, optionsSchemaList);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setOptions(TestHelper.buildOptionsDTOList());

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  /**
   * Patch product with options schema. when product requestTypes is removed and options[] and data
   * entitlements[] have that requestType
   */
  @Test
  public void testValidateOptionsSchema5() throws IOException {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    List<Map<String, Object>> optionsSchemaList = new ArrayList<>();
    Map<String, Object> optionsSchema = new HashMap<>();

    optionsSchema.put(REQUEST_TYPES_ATTRIBUTE, Arrays.asList("NEWREQUEST"));
    optionsSchema.put(SCHEMA, TestHelper.getFileContent("options_schema.json"));
    optionsSchemaList.add(optionsSchema);
    productMap.put(OPTIONS, optionsSchemaList);
    productMap.put(REQUEST_TYPES_ATTRIBUTE, Arrays.asList("NEW_ORDER"));

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setOptions(TestHelper.buildOptionsDTOList());

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    assertEquals(2, errors.getFieldErrors().size());
    /* Assert test results */
    assertTrue(
        errors
                .getFieldErrors()
                .get(0)
                .getCode()
                .contains("synkrato.services.partner.product.requestTypes.delete.invalid")
            && errors.getFieldErrors().get(0).getField().equals(OPTIONS));

    assertTrue(
        errors
                .getFieldErrors()
                .get(1)
                .getCode()
                .contains("synkrato.services.partner.product.requestTypes.delete.invalid")
            && errors.getFieldErrors().get(1).getField().equals(DATA_ENTITLEMENTS));
  }

  /**
   * Patch product with data entitlements. when product requestTypes is removed and data
   * entitlements transactions[] has that requestType
   */
  @Test
  public void testValidateDataEntitlements1() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(REQUEST_TYPES_ATTRIBUTE, Arrays.asList("NEW_ORDER"));

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();

    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    assertEquals(1, errors.getFieldErrors().size());

    /* Assert test results */
    assertTrue(
        errors
                .getFieldErrors()
                .get(0)
                .getCode()
                .contains("synkrato.services.partner.product.requestTypes.delete.invalid")
            && errors.getFieldErrors().get(0).getField().equals(DATA_ENTITLEMENTS));
  }

  /** Internal admin user should not allow to update the status to deprecated */
  @Test
  public void testValidateStatusUpdate_InternalAdmin() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(STATUS, "deprecated");
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals(STATUS, errors.getFieldError().getField());
  }

  /** Partner admin user should not allow to update the status to deprecated */
  @Test
  public void testValidateStatusUpdate_Partner() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(STATUS, "deprecated");
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
    assertEquals(STATUS, errors.getFieldError().getField());
  }

  /** Biz dev user should be allowed to update the status to deprecated */
  @Test
  public void testValidateStatusUpdate_BizDevUser_Deprecated() {
    /* Build Test Data */
    TestHelper.buildBizDevUserJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(STATUS, "deprecated");
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  /** Biz dev user should not be allowed to update the status to development */
  @Test
  public void testValidateStatusUpdate_BizDevUser_Development() {
    /* Build Test Data */
    TestHelper.buildBizDevUserJwt();
    Map<String, Object> productMap = new HashMap<>();
    productMap.put(STATUS, "development");
    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    /* Set up Mock Objects */
    when(messageUtil.getMessage("synkrato.services.partner.product.status.update.error"))
        .thenReturn("synkrato.services.partner.product.status.update.error");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */

    boolean hasException = false;
    try {
      ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);
    } catch (EpcRuntimeException e) {
      if (e.getDetails().equals("synkrato.services.partner.product.status.update.error"))
        hasException = true;
    }
    /* Assert Test Results */

    Assert.assertTrue(hasException);
  }

  @Test
  public void testResponseResources() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();

    List<String> resources = Arrays.asList(RandomStringUtils.random(300));
    Map<String, Object> response = new HashMap<>();
    response.put("resources", resources);

    Map<String, Object> transaction = new HashMap<>();
    transaction.put("response", response);

    List<Map<String, Object>> transactions = Arrays.asList(transaction);

    Map<String, Object> data = new HashMap<>();
    data.put("transactions", transactions);

    Map<String, Object> entitlements = new HashMap<>();
    entitlements.put("data", data);

    Map<String, Object> productMap = new HashMap<>();
    productMap.put("entitlements", entitlements);

    // Existing Product
    ProductDTO existingProductDto = new ProductDTO();
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> preMergeMap = new HashMap<>();
    preMergeMap.put("updateProductMap", productMap);
    preMergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, preMergeMap, errors);

    /* Assert test results */
    assertEquals(2, errors.getFieldErrors().size());
    assertEquals(
        "entitlements.data.transactions[0].response.resources[0]",
        errors.getFieldErrors().get(1).getField());
  }

  /** when resources is null, no error should be thrown */
  @Test
  public void testResponseResourcesNullTest() {
    /* Build Test Data */
    TestHelper.buildApplicationJWT();

    Map<String, Object> response = new HashMap<>();
    response.put("resources", null);

    Map<String, Object> transaction = new HashMap<>();
    transaction.put("response", response);

    List<Map<String, Object>> transactions = Arrays.asList(transaction);

    Map<String, Object> data = new HashMap<>();
    data.put("transactions", transactions);

    Map<String, Object> entitlements = new HashMap<>();
    entitlements.put("data", data);

    Map<String, Object> productMap = new HashMap<>();
    productMap.put("entitlements", entitlements);

    // Existing Product
    ProductDTO existingProductDto = new ProductDTO();
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> preMergeMap = new HashMap<>();
    preMergeMap.put("updateProductMap", productMap);
    preMergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, preMergeMap, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  /** Bad request, if there are constraints validations with workflows as null */
  @Test
  public void testValidateConstraintsOnTags() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    Map<String, Object> tagsMap = new HashMap<>();
    tagsMap.put(WORKFLOWS, null);
    productMap.put(TAG_ATTRIBUTE, tagsMap);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.development);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.notEmpty"));
    assertEquals(TAG_ATTRIBUTE, errors.getFieldError().getField());
  }

  /** Bad request, if there are constraints validations with valid workflows */
  @Test
  public void testValidateNoConstraintsOnTags() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = new HashMap<>();
    Map<String, Object> tagsMap = new HashMap<>();
    tagsMap.put(WORKFLOWS, Arrays.asList("inteactive"));
    productMap.put(TAG_ATTRIBUTE, tagsMap);

    // Existing Product
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    existingProductDto.setStatus(ProductStatusType.development);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertNull(errors.getFieldError());
  }

  /** Partner failed to update approved product - interfaceUrl, adminInterfaceUrl, webhooks */
  @Test
  public void testValidateReadonlyAttributeFromPartner() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.put("status", "approved");
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setStatus(ProductStatusType.approved);
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("listingName", "testListingName");
    productMap.put("interfaceUrl", "interfaceUrl");
    productMap.put("adminInterfaceUrl", "adminInterfaceUrl");

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));

    assertNotNull(errors.getFieldError("interfaceUrl").getCode());
    assertNotNull(errors.getFieldError("adminInterfaceUrl").getCode());
    assertNotNull(errors.getFieldError("webhooks").getCode());
  }

  /** Lender user can update approved product - interfaceUrl, adminInterfaceUrl, webhooks */
  @Test
  public void testValidateReadonlyAttributeFromLender() {
    /* Build Test Data */
    TestHelper.buildLenderJWT();
    Map<String, Object> productMap = TestHelper.buildAsyncProductMap();
    productMap.put("status", "approved");
    productMap.remove(ENTITLEMENTS);
    ProductDTO existingProductDto = TestHelper.buildProductDTO();
    existingProductDto.setStatus(ProductStatusType.approved);
    existingProductDto.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(existingProductDto, "Product");

    Map<String, Object> premergeMap = new HashMap<>();
    premergeMap.put("updateProductMap", productMap);
    premergeMap.put("existingProductDTO", existingProductDto);

    productMap.put("listingName", "testListingName");
    productMap.put("interfaceUrl", "interfaceUrl");
    productMap.put("adminInterfaceUrl", "adminInterfaceUrl");

    /* Execute Test */
    ValidationUtils.invokeValidator(productUpdateValidator, premergeMap, errors);

    /* Assert test results */
    assertNull(errors.getFieldError());
    assertNull(errors.getFieldError("interfaceUrl"));
    assertNull(errors.getFieldError("adminInterfaceUrl"));
    assertNull(errors.getFieldError("webhooks"));
  }
}
