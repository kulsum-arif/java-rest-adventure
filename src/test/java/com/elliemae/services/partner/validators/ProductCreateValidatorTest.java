package com.synkrato.services.partner.validators;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.enums.IntegrationType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.ProductService;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import com.synkrato.services.partner.util.ProductUtil;
import com.synkrato.services.partner.util.TestHelper;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProductCreateValidatorTest {
  @Mock ProductService productService;
  @Mock MessageUtil messageUtil;
  @Mock ProductUtil productUtil;
  @InjectMocks ProductValidator productValidatorRootMock;
  @Mock ProductValidator productValidator;
  @InjectMocks ProductCreateValidator productCreateValidator;

  ProductSearchDTO searchAttribute =
      ProductSearchDTO.builder()
          .partnerId(TestHelper.TEST_PARTNER_ID)
          .name(TestHelper.TEST_PRODUCT_NAME)
          .build();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  /** Create Product validation is successful without any duplicate Products */

  // Actor: Anyone JWT
  // Action: Create Product with Duplicate ID and name
  // Expected Result: Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test1ValidateDuplicateProductSuccess() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    List<ProductDTO> productList = new ArrayList<>();
    productList.add(productDTO);
    productDTO.setStatus(null);
    productDTO.setEnvironment(null);
    productDTO.getEntitlements().setData(null);
    productDTO.setExtensionLimit(null);
    productDTO.setIntegrationType(IntegrationType.ASYNC);
    productDTO.getWebhooks().get(0).setSubscriptionId(null);

    when(productService.findByPartnerIdAndName(productDTO.getPartnerId(), productDTO.getName()))
        .thenReturn(productList);
    /* Execute Test */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    ValidationUtils.invokeValidator(productCreateValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.duplicate.error"));
  }

  // Actor: Anyone JWT
  // Action: Create Product with Empty Webhook
  // Expected Result: Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test2ValidateEmptyWebhookSuccess() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);

    productDTO.setIntegrationType(IntegrationType.ASYNC);
    productDTO.setWebhooks(null);
    productDTO.getEntitlements().setData(null);

    ValidationUtils.invokeValidator(productCreateValidator, productDTO, errors);
    /* Execute Test */
    /*validateEmptyWebhook(
    productDTO, productDTO.getIntegrationType(), errors);*/
    /* Assert test results */

    productCreateValidator.validate(productDTO, errors);

    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.webhook.required"));
  }

  // Actor: Anyone JWT
  // Action: Create Product with Duplicate ID and name
  // Expected Result: Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test3ValidateReadOnlyAttributes() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    productDTO.setEnvironment(null);
    productDTO.getEntitlements().setData(null);
    productDTO.setExtensionLimit(null);
    productDTO.setIntegrationType(IntegrationType.ASYNC);
    productDTO.getWebhooks().get(0).setSubscriptionId(null);

    when(productUtil.isValidDataType(any(), any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productCreateValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
  }
  // Actor: Internal Admin JWT
  // Action: Create Product without passing partnerId
  // Expected Result: Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test4ValidateWithInternalAdminJWT() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setPartnerId(null);
    Errors errors = new BindException(productDTO, "Product");

    when(productUtil.isValidDataType(any(), any())).thenReturn(true);

    ValidationUtils.invokeValidator(productCreateValidator, productDTO, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.partner-id.required"));
  }
  // Actor: Anyone JWT
  // Action: Create Product with null request type
  // Expected Result: Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test5ValidateRequestTypes() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    productDTO.setRequestTypes(null);
    productDTO.getEntitlements().setData(null);
    productDTO.setIntegrationType(IntegrationType.ASYNC);
    productDTO.getWebhooks().get(0).setSubscriptionId(null);

    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    ValidationUtils.invokeValidator(productCreateValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(5, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.request-types.required"));
  }

  // Actor: Partner JWT
  // Action: Create Product with Empty Request Type
  // Expected Result: Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test6ValidateRequestTypesFailure() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setStatus(null);
    productDTO.setEnvironment(null);
    productDTO.getEntitlements().setData(null);
    productDTO.setExtensionLimit(null);
    productDTO.setIntegrationType(IntegrationType.ASYNC);

    List<String> requestTypeList = new ArrayList<>();
    requestTypeList.add("SEARCH");
    requestTypeList.add("NEWREQUEST");
    requestTypeList.add("NEWREQUEST");
    requestTypeList.add("SEARCH");
    productDTO.setRequestTypes(requestTypeList);
    productDTO.getWebhooks().get(0).setSubscriptionId(null);

    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(false);
    /* Execute Test */
    ValidationUtils.invokeValidator(productCreateValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    // assertTrue(errors.getFieldErrors().size() > 0);
  }

  // Actor: Partner JWT
  // Action: Create Product with Valid Request Type
  // Expected Result: Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test7ValidateRequestTypesPass() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setStatus(null);
    productDTO.setEnvironment(null);
    productDTO.getEntitlements().setData(null);
    productDTO.setExtensionLimit(null);
    productDTO.setIntegrationType(IntegrationType.ASYNC);
    productDTO.getWebhooks().get(0).setSubscriptionId(null);

    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(false);
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productCreateValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor: Partner JWT
  // Action: Create Product with invalid integration type
  // Expected Result: Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test8ValidateIntegrationType() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setIntegrationType(null);
    productDTO.getEntitlements().setData(null);
    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productCreateValidator, productDTO, errors);

    /* Assert test results */

    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
  }
  // Actor: Partner JWT
  // Action: Create Product with entitlements.data
  // Expected Result: 400
  @Test
  public void test8ValidateDataEntitlements() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setIntegrationType(null);
    Errors errors = new BindException(productDTO, "Product");
    boolean exception = false;
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    try {
      ValidationUtils.invokeValidator(productCreateValidator, productDTO, errors);
    } catch (Exception e) {
      exception = true;
    }
    /* Assert test results */

    assertTrue(exception);
  }

  /** This test is to validate subscriptionId is a readonly attribute in product creation */
  @Test
  public void testSubscriptionIdOnCreateProduct() {

    // Arrange
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    Errors errors = new BindException(productDTO, "Product");
    HttpStatus httpStatus = null;

    // Mock
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);

    // Act
    try {
      ValidationUtils.invokeValidator(productCreateValidator, productDTO, errors);
    } catch (EpcRuntimeException e) {
      httpStatus = e.getHttpStatus();
    }

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, httpStatus);
    assertTrue(
        errors.getFieldError().getCode().contains("synkrato.services.partner.product.readonly"));
  }
}
