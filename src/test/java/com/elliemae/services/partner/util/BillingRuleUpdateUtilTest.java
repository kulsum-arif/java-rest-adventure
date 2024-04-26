package com.synkrato.services.partner.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.JsonSchemaValidationUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.cache.S3ClientCache;
import com.synkrato.services.partner.data.Product;
import com.synkrato.services.partner.validators.BillingRuleUpdateValidator;
import com.synkrato.services.partner.validators.BillingRuleValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

public class BillingRuleUpdateUtilTest {
  @InjectMocks BillingRuleUpdateUtil billingRuleUpdateUtil;
  @Spy BillingRuleUpdateValidator billingRuleUpdateValidator;
  @Spy BillingRuleValidator billingRuleValidator;
  @Spy ObjectMapper objectMapper;
  @Spy MessageUtil messageUtil;
  @Mock S3ClientCache s3client;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    ObjectMapper objectMapper = new ObjectMapper();

    ReflectionTestUtils.setField(billingRuleUpdateValidator, "messageUtil", messageUtil);
    ReflectionTestUtils.setField(
        billingRuleValidator, "jsonSchemaValidationUtil", new JsonSchemaValidationUtil());
    ReflectionTestUtils.setField(billingRuleValidator, "bucketName", "test-bucket");
    ReflectionTestUtils.setField(billingRuleValidator, "prefix", "test");
    ReflectionTestUtils.setField(billingRuleValidator, "s3client", s3client);
  }

  /** test premerge validation success */
  @Test
  public void premergeValidationSuccess() {
    boolean success = true;

    // Arrange
    TestHelper.buildPartnerJWT();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    productDTO.setId(product.getId().toString());
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    UUID billingRuleID = UUID.randomUUID();
    billingRuleDTO.setId(billingRuleID.toString());

    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});
    billingRuleDTO.setProductId(product.getId().toString());

    // Act
    try {
      billingRuleUpdateUtil.preMergeValidate(billingRuleMap, billingRuleDTO);
    } catch (Exception ex) {
      success = false;
    }

    // Assert
    assertTrue(success);
  }

  /** test premerge validation fail on passing readonly fields */
  @Test(expected = Exception.class)
  public void premergeValidationWithReadOnlyFieldsFailure() throws Exception {

    // Arrange
    TestHelper.buildPartnerJWT();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    productDTO.setId(product.getId().toString());
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();

    UUID billingRuleID = UUID.randomUUID();
    billingRuleDTO.setId(billingRuleID.toString());

    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});
    billingRuleMap.put("id", UUID.randomUUID().toString());
    billingRuleDTO.setProductId(product.getId().toString());

    // Act
    billingRuleUpdateUtil.preMergeValidate(billingRuleMap, billingRuleDTO);
  }

  /** test post merge validation success */
  @Test
  public void postMergeValidationSuccess() throws Exception {

    // Arrange
    TestHelper.buildPartnerJWT();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    productDTO.setId(product.getId().toString());
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();

    UUID billingRuleID = UUID.randomUUID();
    billingRuleDTO.setId(billingRuleID.toString());

    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});

    billingRuleDTO.setProductId(product.getId().toString());

    JSONObject schema = new JSONObject(TestHelper.getBillingRuleSchema());

    // Mock
    when(s3client.getS3ObjectContent(anyString(), anyString(), anyString())).thenReturn(schema);

    // Act
    billingRuleUpdateUtil.postMergeValidate(billingRuleDTO);
  }

  /** test postmerge validation failure */
  @Test(expected = Exception.class)
  public void postMergeValidationFailure() throws Exception {

    // Arrange
    TestHelper.buildPartnerJWT();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    productDTO.setId(product.getId().toString());
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();

    UUID billingRuleID = UUID.randomUUID();
    billingRuleDTO.setId(billingRuleID.toString());

    billingRuleDTO.getTransformations().get(0).getTransaction().remove("requestType");

    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});

    billingRuleDTO.setProductId(product.getId().toString());

    JSONObject schema = new JSONObject(TestHelper.getBillingRuleSchema());

    // Mock
    when(s3client.getS3ObjectContent(anyString(), anyString(), anyString())).thenReturn(schema);

    // Act
    billingRuleUpdateUtil.postMergeValidate(billingRuleDTO);
  }

  /** test merge method */
  @Test
  public void mergeSuccess() {

    // Arrange
    TestHelper.buildPartnerJWT();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    productDTO.setId(product.getId().toString());
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();

    UUID billingRuleID = UUID.randomUUID();
    billingRuleDTO.setId(billingRuleID.toString());

    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});

    billingRuleDTO.setProductId(product.getId().toString());

    JSONObject schema = new JSONObject(TestHelper.getBillingRuleSchema());

    // Mock
    when(s3client.getS3ObjectContent(anyString(), anyString(), anyString())).thenReturn(schema);

    // Act
    BillingRuleDTO result = billingRuleUpdateUtil.merge(billingRuleMap, billingRuleDTO);

    // Assert
    assertNotNull(result);
    assertEquals(result.getId(), billingRuleDTO.getId());
  }

  /** test merge method */
  @Test
  public void mergeFailure() throws JsonProcessingException {

    // Arrange
    TestHelper.buildPartnerJWT();

    Map<String, Object> billingRuleMap = new HashMap<>();
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    boolean error = false;

    EpcRuntimeException epcRuntimeException =
        new EpcRuntimeException(HttpStatus.BAD_REQUEST, "invalid content in request");

    // Mock
    doThrow(epcRuntimeException).when(objectMapper).writeValueAsString(any(Object.class));

    try {

      // Act
      BillingRuleDTO result = billingRuleUpdateUtil.merge(billingRuleMap, billingRuleDTO);
      error = true;

    } catch (EpcRuntimeException ex) {
      assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    // Assert
    assertFalse(error);
  }

  @Test
  public void validateIfAuthorizedToUpdateSuccess() {

    // Arrange

    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    boolean result = false;

    try {

      // Act
      billingRuleUpdateValidator.validateIfAuthorizedToUpdate(productDTO);
      result = true;

    } catch (Exception ex) {

    }

    // Assert
    assertTrue(result);
  }

  @Test
  public void validateIfAuthorizedToUpdateWithInternalAdminJWTSuccess() {

    // Arrange

    TestHelper.buildInternalAdminJwt();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    boolean result = false;

    try {

      // Act
      billingRuleUpdateValidator.validateIfAuthorizedToUpdate(productDTO);
      result = true;

    } catch (Exception ex) {

    }

    // Assert
    assertTrue(result);
  }

  @Test
  public void validateIfAuthorizedFailure() {

    // Arrange

    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    boolean result = false;

    try {

      // Act
      billingRuleUpdateValidator.validateIfAuthorizedToUpdate(productDTO);
      result = true;

    } catch (Exception ex) {

    }

    // Assert
    assertFalse(result);
  }
}
