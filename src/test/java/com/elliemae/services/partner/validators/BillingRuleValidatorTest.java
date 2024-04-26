package com.synkrato.services.partner.validators;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.enums.ProductStatusType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.JsonSchemaValidationUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.cache.S3ClientCache;
import com.synkrato.services.partner.util.BillingRuleUtil;
import com.synkrato.services.partner.util.TestHelper;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

@RunWith(MockitoJUnitRunner.class)
public class BillingRuleValidatorTest {

  @Mock MessageUtil messageUtil;
  @Mock BillingRuleUtil billingRuleUtil;
  @Mock S3ClientCache s3client;
  @InjectMocks BillingRuleValidator billingRuleValidator;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(
        billingRuleValidator, "jsonSchemaValidationUtil", new JsonSchemaValidationUtil());
    ReflectionTestUtils.setField(billingRuleValidator, "bucketName", "test-bucket");
    ReflectionTestUtils.setField(billingRuleValidator, "prefix", "test");
  }

  /** This test is a positive test case for valid BillingRule DTO */
  @Test
  public void createBillingRuleValid() {

    // Arrange
    TestHelper.buildApplicationJWT();
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    Errors errors = new BindException(billingRuleDTO, "BillingRule");
    JSONObject schema = new JSONObject(TestHelper.getBillingRuleSchema());

    // Mock
    when(s3client.getS3ObjectContent(anyString(), anyString(), anyString())).thenReturn(schema);

    // Act
    ValidationUtils.invokeValidator(billingRuleValidator, billingRuleDTO, errors);

    // Assert
    assertEquals(0, errors.getFieldErrors().size());
  }

  /**
   * This tests a billingRule containing multiple transformations with the same sku defined for
   * each.
   */
  @Test
  public void createBillingRuleWithSameSkusInTransformation() {

    // Arrange
    TestHelper.buildApplicationJWT();
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.getTransformations().add(billingRuleDTO.getTransformations().get(0));
    Errors errors = new BindException(billingRuleDTO, "BillingRule");
    JSONObject schema = new JSONObject(TestHelper.getBillingRuleSchema());

    // Mock
    when(s3client.getS3ObjectContent(anyString(), anyString(), anyString())).thenReturn(schema);

    // Act
    ValidationUtils.invokeValidator(billingRuleValidator, billingRuleDTO, errors);

    // Assert
    assertEquals(0, errors.getFieldErrors().size());
  }

  /**
   * This test is a negative test case where a billingRule contains a groupingRule that has the same
   * sku defined as the transformation sku at the root level
   */
  @Test
  public void createBillingRuleWithSameSkusInGroupingRules() {

    // Arrange
    TestHelper.buildApplicationJWT();
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO
        .getTransformations()
        .get(0)
        .getGroupingRules()
        .get(0)
        .put("groupAs", billingRuleDTO.getTransformations().get(0).getSku());
    Errors errors = new BindException(billingRuleDTO, "BillingRule");
    JSONObject schema = new JSONObject(TestHelper.getBillingRuleSchema());

    // Mock
    when(s3client.getS3ObjectContent(anyString(), anyString(), anyString())).thenReturn(schema);

    // Act
    ValidationUtils.invokeValidator(billingRuleValidator, billingRuleDTO, errors);

    // Assert
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.billing.transformations.sku.duplicate"));
  }

  /**
   * This test is a negative test case where a billingRule contains groupingRules that have
   * duplicate skus defined
   */
  @Test
  public void createBillingRuleWithDuplicateSkusInGroupingRules() {

    // Arrange
    TestHelper.buildApplicationJWT();
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO
        .getTransformations()
        .get(0)
        .getGroupingRules()
        .add(billingRuleDTO.getTransformations().get(0).getGroupingRules().get(0));
    Errors errors = new BindException(billingRuleDTO, "BillingRule");
    JSONObject schema = new JSONObject(TestHelper.getBillingRuleSchema());

    // Mock
    when(s3client.getS3ObjectContent(anyString(), anyString(), anyString())).thenReturn(schema);

    // Act
    ValidationUtils.invokeValidator(billingRuleValidator, billingRuleDTO, errors);

    // Assert
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains(
                "synkrato.services.partner.product.billing.transformations.groupingRules.sku.duplicate"));
  }

  /** This test is a negative test case where a billingRule contains invalid transformations */
  @Test
  public void createBillingRuleWithInvalidTransformations() {

    // Arrange
    TestHelper.buildApplicationJWT();
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.getTransformations().get(0).getGroupingRules().get(0).remove("groupAs");
    Errors errors = new BindException(billingRuleDTO, "BillingRule");
    JSONObject schema = new JSONObject(TestHelper.getBillingRuleSchema());

    // Mock
    when(s3client.getS3ObjectContent(anyString(), anyString(), anyString())).thenReturn(schema);

    // Act
    ValidationUtils.invokeValidator(billingRuleValidator, billingRuleDTO, errors);

    // Assert
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.billing.transformations.invalid"));
  }

  /** This method will validate if the product is not deprecated */
  @Test(expected = EpcRuntimeException.class)
  public void validateProductTest() {
    // Arrange
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setStatus(ProductStatusType.deprecated);

    // Act
    billingRuleValidator.validateProduct(productDTO);
  }

  /** This method will check for invalid request types */
  @Test(expected = EpcRuntimeException.class)
  public void validateBillingRuleForProductTest() {
    // Arrange
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO
        .getTransformations()
        .get(0)
        .getTransaction()
        .put("requestType", "invalid_request_type");

    // Act
    billingRuleValidator.validateBillingRuleForProduct(productDTO, billingRuleDTO);
  }

  /** This method will check for invalid request types */
  @Test(expected = EpcRuntimeException.class)
  public void validateBillingRuleForRequestTypes() {
    // Arrange
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    List<String> requestTypes = new ArrayList<>();
    requestTypes.add("invalid");
    billingRuleDTO.getTransformations().get(0).getTransaction().put("requestTypes", requestTypes);

    // Act
    billingRuleValidator.validateBillingRuleForProduct(productDTO, billingRuleDTO);
  }

  /** This method will check for null or empty options */
  @Test
  public void validateBillingRuleWithNullOptionsInTransactionTest() {
    // Arrange
    TestHelper.buildApplicationJWT();
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.getTransformations().get(0).getTransaction().put("options", null);
    Errors errors = new BindException(billingRuleDTO, "BillingRule");
    JSONObject schema = new JSONObject(TestHelper.getBillingRuleSchema());

    // Mock
    when(s3client.getS3ObjectContent(anyString(), anyString(), anyString())).thenReturn(schema);

    // Act
    ValidationUtils.invokeValidator(billingRuleValidator, billingRuleDTO, errors);

    // Assert
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.attributes.notEmpty"));
  }
}
