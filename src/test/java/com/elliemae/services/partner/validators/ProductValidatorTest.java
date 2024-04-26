package com.synkrato.services.partner.validators;

import static com.synkrato.services.partner.PartnerServiceConstants.AUS24_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.CD33_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.CLOSING231_LOCK_FORM_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.CLOSING_EXTENDED_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.FANNIE_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.FREDDIE_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.ILAD_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.LE33_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.LOAN_DELIVERY_FANNIE_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.PROPERTIES_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.SCHEMA_URI_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.TYPE_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.UCD_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.UCD_FINAL_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.ULADDU_DOC_TYPE;
import static com.synkrato.services.partner.PartnerServiceConstants.ULADPA_DOC_TYPE;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.synkrato.services.epc.common.dto.AdditionalLinkDTO;
import com.synkrato.services.epc.common.dto.ConditionDTO;
import com.synkrato.services.epc.common.dto.EntitlementDTO;
import com.synkrato.services.epc.common.dto.ExportDTO;
import com.synkrato.services.epc.common.dto.FeatureDTO;
import com.synkrato.services.epc.common.dto.FieldDTO;
import com.synkrato.services.epc.common.dto.FindingDTO;
import com.synkrato.services.epc.common.dto.FindingTypeDTO;
import com.synkrato.services.epc.common.dto.ManifestDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.ServiceEventDTO;
import com.synkrato.services.epc.common.dto.ServiceEventTypeDTO;
import com.synkrato.services.epc.common.dto.WebhookDTO;
import com.synkrato.services.epc.common.dto.enums.AdditionalLinkType;
import com.synkrato.services.epc.common.dto.enums.IntegrationType;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.ProductService;
import com.synkrato.services.partner.cache.S3ClientCache;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import com.synkrato.services.partner.util.ProductUtil;
import com.synkrato.services.partner.util.TestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RunWith(MockitoJUnitRunner.class)
public class ProductValidatorTest {

  @Mock ProductService productService;
  @Mock MessageUtil messageUtil;
  @Mock ProductUtil productUtil;
  @Mock S3ClientCache s3ClientCache;
  @InjectMocks ProductValidator productValidator;
  @Spy ObjectMapper objectMapper;

  ProductSearchDTO searchAttribute =
      ProductSearchDTO.builder()
          .partnerId(TestHelper.TEST_PARTNER_ID)
          .name(TestHelper.TEST_PRODUCT_NAME)
          .build();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(productValidator, "objectMapper", objectMapper);
  }

  // Actor:  Valid Environment Claim in JWT
  // Action: Create Product
  // Expected Result:  Success ( errors.getFieldErrors().size() == 0)
  @Test
  public void test1ValidatePartnerEnvironmentSuccess() {
    /* Build Test Data */
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor:  Invalid Environment claim in JWT
  // Action: Create Product
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test2ValidatePartnerEnvironmentFailure() {
    /* Build Test Data */
    TestHelper.buildInvalidEnvironmentPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.environment.invalid"));
  }

  // Actor:  Anyone
  // Action: Create Product with duplicate request type
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test3ValidateDuplicateRequestTypeFailure() throws Exception {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    List<String> requestTypeList = new ArrayList<>();
    requestTypeList.add("SEARCH");
    requestTypeList.add("NEWREQUEST");
    requestTypeList.add("NEWREQUEST");
    requestTypeList.add("SEARCH");
    productDTO.setRequestTypes(requestTypeList);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    MethodParameter methodParameter = mock(MethodParameter.class);

    /* Execute Test */
    String fieldId = null;
    String errorCode = null;
    try {
      ValidationUtils.invokeValidator(productValidator, productDTO, errors);
      productValidator.handleErrors(methodParameter, (BindingResult) errors);
    } catch (MethodArgumentNotValidException e) {
      errorCode = e.getBindingResult().getFieldErrors().get(0).getCode();
      fieldId = e.getBindingResult().getFieldErrors().get(0).getField();
    }
    /* Assert test results */
    assertEquals("synkrato.services.partner.product.request-types.duplicate", errorCode);
    assertEquals("requestTypes", fieldId);
  }

  // Actor:  Anyone
  // Action: Create Product with Unique request type
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 0)
  @Test
  public void test4ValidateDuplicateRequestTypeSuccess() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    List<String> requestTypeList = new ArrayList<>();
    requestTypeList.add("SEARCH");
    requestTypeList.add("NEWREQUEST");
    productDTO.setRequestTypes(requestTypeList);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor:  Anyone
  // Action: Create Product with empty string request type
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test5ValidateEmptyRequestTypeFailure() throws Exception {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    List<String> requestTypeList = new ArrayList<>();
    requestTypeList.add("SEARCH");
    requestTypeList.add("");
    productDTO.setRequestTypes(requestTypeList);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    MethodParameter methodParameter = mock(MethodParameter.class);

    /* Execute Test */
    String errorCode = null;
    String fieldId = null;
    try {
      ValidationUtils.invokeValidator(productValidator, productDTO, errors);
      productValidator.handleErrors(methodParameter, (BindingResult) errors);
    } catch (MethodArgumentNotValidException e) {
      errorCode = e.getBindingResult().getFieldErrors().get(0).getCode();
      fieldId = e.getBindingResult().getFieldErrors().get(0).getField();
    }
    /* Assert test results */
    assertEquals("synkrato.services.partner.product.request-types.empty", errorCode);
    assertEquals("requestTypes", fieldId);
  }

  // Actor:  Anyone
  // Action: Create Product with no child in object
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test6ValidateEmptyRequestTypeFailure() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    List<String> requestTypeList = new ArrayList<>();
    productDTO.setRequestTypes(requestTypeList);
    Errors errors = new BindException(productDTO, "Product");

    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.request-types.required"));
  }

  // Actor:  Anyone
  // Action: Create Product with no partnerUrl for IntegrationType = P2P
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test7ValidatePartnerUrlFailure() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setIntegrationType(IntegrationType.P2P);
    productDTO.setPartnerUrl(null);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldError()
            .getCode()
            .contains("synkrato.services.partner.product.partner-url.required"));
  }

  // Actor:  Partner Or Application
  // Action: Create Product with no partnerUrl for IntegrationType = ASYNC
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)

  @Test
  public void test8ValidateWebhookSuccess() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor:  Partner Or Application
  // Action: Create Product with empty webhook URL, and resoureType
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 2)
  @Test
  public void test9ValidateWebhookFailure() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setIntegrationType(IntegrationType.ASYNC);
    productDTO.setWebhooks(TestHelper.buildWebhookDTO());

    WebhookDTO webhookDTO = new WebhookDTO();
    webhookDTO.setUrl("");
    webhookDTO.setSigningkey("BQ1dOoYdb52Lkjsrhei7PjAf3*gGARdg8S3@fz2UAA1gRn^bKcRU");
    webhookDTO.setResource("urn:elli:epc:transaction");
    List<WebhookDTO> webhookDTOList = new ArrayList<>();
    webhookDTOList.add(webhookDTO);

    productDTO.setWebhooks(webhookDTOList);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(2, errors.getFieldErrors().size());
  }

  // Actor:  Partner Or Application
  // Action: Create Product with empty webhook URL, event and resoureType
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 3)
  @Test
  public void test10ValidateWebhookAttributesFailure() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setIntegrationType(IntegrationType.ASYNC);
    productDTO.setWebhooks(TestHelper.buildWebhookDTO());

    WebhookDTO webhookDTO = new WebhookDTO();
    webhookDTO.setUrl("");
    webhookDTO.setSigningkey("BQ1dOoYdb52Lkjsrhei7PjAf3*gGARdg8S3@fz2UAA1gRn^bKcRU");
    webhookDTO.setEvents(null);
    webhookDTO.setResource("");
    List<WebhookDTO> webhookDTOList = new ArrayList<>();
    webhookDTOList.add(webhookDTO);

    productDTO.setWebhooks(webhookDTOList);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(3, errors.getFieldErrors().size());
  }

  // Actor:  Partner Or Application
  // Action: Create Product with all webhook details
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 0)
  @Test
  public void test11ValidateWebhookAttributesSuccess() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setIntegrationType(IntegrationType.ASYNC);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor:  Partner Or Application
  // Action: Create Product with Duplicate resource
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 3)
  @Test
  public void test12CheckDuplicateWebhookSubscriptionsFailure() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setIntegrationType(IntegrationType.ASYNC);
    productDTO.setWebhooks(TestHelper.buildWebhookDTO());

    WebhookDTO webhookDTO1 = new WebhookDTO();
    webhookDTO1.setUrl("https://dummywebhook.site/1");
    webhookDTO1.setSigningkey("BQ1dOoYdb52Lkjsrhei7PjAf3*gGARdg8S3@fz2UAA1gRn^bKcRU");
    webhookDTO1.setResource("urn:elli:epc:transaction");
    List<String> events1 = new ArrayList<>();
    events1.add("created");
    events1.add("updated");
    webhookDTO1.setEvents(events1);

    WebhookDTO webhookDTO2 = new WebhookDTO();
    webhookDTO2.setUrl("https://dummywebhook.site/1");
    webhookDTO2.setSigningkey("BQ1dOoYdb52Lkjsrhei7PjAf3*gGARdg8S3@fz2UAA1gRn^bKcRU");
    webhookDTO2.setResource("urn:elli:epc:transaction");
    List<String> events2 = new ArrayList<>();
    events1.add("created");
    events1.add("updated");
    webhookDTO2.setEvents(events1);

    List<WebhookDTO> webhookDTOList = new ArrayList<>();
    webhookDTOList.add(webhookDTO1);
    webhookDTOList.add(webhookDTO2);

    productDTO.setWebhooks(webhookDTOList);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
  }

  // Actor:  Partner JWT
  // Action: Create Product with data Entitlement
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)

  @Test
  public void test13ValidateDataEntitlementsWithDataFailure() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.getEntitlements().setData(new ManifestDTO());
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertTrue(errors.getFieldErrors().get(0).getField().contains("entitlements.data"));
  }

  // Actor:  Partner JWT
  // Action: Create Product without data Entitlement
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 0)
  @Test
  public void test14ValidateDataEntitlementsWithoutDataSuccess() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setEntitlements(null);
    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor:  Application JWT
  // Action: Create Product with data Entitlement
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 0)
  @Test
  public void test15ValidateDataEntitlementsWithDataSuccess() {
    /* Build Test Data */
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor:  Internal Admin JWT
  // Action: Create Product with data Entitlement
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 0)
  @Test
  public void test16ValidateDataEntitlementsWithDataSuccessForInternalAdmin() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor:  Application JWT
  // Action: Create Product without Origin data Entitlement
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test17ValidateOriginDataEntitlementsSuccess() {

    /* Build Test Data */
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    // productDTO.setEntitlements(null);
    EntitlementDTO entitlementDTO = new EntitlementDTO();
    ManifestDTO manifestDTO = TestHelper.buildManifestDTO("21212");
    manifestDTO.setOrigin(null);
    entitlementDTO.setData(manifestDTO);
    productDTO.setEntitlements(entitlementDTO);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
  }

  // Actor:  Application JWT
  // Action: Create Product without Request data Entitlement
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test18ValidateRequestDataEntitlementsSuccess() {

    /* Build Test Data */
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    EntitlementDTO entitlementDTO = new EntitlementDTO();
    ManifestDTO manifestDTO = TestHelper.buildManifestDTO("21212");
    manifestDTO.setTransactions(TestHelper.buildTransactionEntitlementDTOList("SEARCH"));
    manifestDTO.getTransactions().get(0).setRequest(null);
    entitlementDTO.setData(manifestDTO);
    productDTO.setEntitlements(entitlementDTO);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
  }

  // Actor:  Application JWT
  // Action: Create Product without Response data Entitlement
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test19ValidateResponseDataEntitlementsSuccess() {

    /* Build Test Data */
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    // productDTO.setEntitlements(null);
    EntitlementDTO entitlementDTO = new EntitlementDTO();
    ManifestDTO manifestDTO = TestHelper.buildManifestDTO("21212");
    manifestDTO.setTransactions(null);
    entitlementDTO.setData(manifestDTO);
    productDTO.setEntitlements(entitlementDTO);
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
  }

  @Test
  public void test20CredentialValidationSuccess() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    TestHelper.buildPartnerJWT();

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(false);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  /** Create Product validation is successful without any duplicate Products */
  @Test
  public void test21ValidateProduct() {
    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    TestHelper.buildPartnerJWT();
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor:  Application JWT
  // Action: Create Product with Response data Entitlement (Origin, Transaction - request, response)
  // Expected Result:  Success ( errors.getFieldErrors().size() == 0)
  @Test
  public void test22ValidateDataEntitlementsJsonPath() {

    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor:  Application JWT
  // Action: Create Product with Response data Entitlement (Origin, Transaction - request, response)
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test23ValidateOriginDataEntitlementsJsonPathFailure() {

    /* Build Test Data */
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getOrigin()
        .setFields(TestHelper.buildManifestInvalidJsonPathFieldDTO());

    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
  }

  // Actor:  Application JWT
  // Action: Create Product with Response data Entitlement (Origin, Transaction - request, response)
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test24ValidateTransactionRequestDataEntitlementsJsonPathFailure() {

    /* Build Test Data */
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .setFields(TestHelper.buildManifestInvalidJsonPathFieldDTO());

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .setConditions(null);

    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
  }

  // Actor:  Application JWT
  // Action: Create Product with Response data Entitlement (Origin, Transaction - request, response)
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test25ValidateTransactionResponseDataEntitlementsJsonPathFailure() {

    /* Build Test Data */
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .setFields(TestHelper.buildManifestInvalidJsonPathFieldDTO());

    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
  }

  // Actor:  Internal Admin JWT
  // Action: Create Product with Response data Entitlement (Origin, Transaction - request,
  // response), transaction request json path has null value
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test26ValidateTransactionResponseDataEntitlementsJsonPathFailure() {

    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getFields()
        .add(FieldDTO.builder().build());

    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCodes()[0]
            .contains("synkrato.services.partner.product.entitlements.data.json-path.empty"));
  }

  // Actor:  Internal Admin JWT
  // Action: Create Product with Response data Entitlement (Origin, Transaction - request,
  // response), transaction request json path has empty spaces
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)
  @Test
  public void test27ValidateTransactionResponseDataEntitlementsJsonPathFailure() {

    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getFields()
        .add(FieldDTO.builder().jsonPath("  ").build());

    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCodes()[0]
            .contains(
                "synkrato.services.partner.product.entitlements.data.json-path.invalid.Product.entitlements.data"));
  }

  /**
   * Actor: Application JWT Action: Create Product with Origin data Entitlement with vector json
   * paths Expected Result: Fail ( errors.getFieldErrors().size() == 2)
   */
  @Test
  public void test28ValidateTransactionResponseDataEntitlementsJsonPathFailure() {

    /* Build Test Data */
    TestHelper.buildApplicationJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getOrigin()
        .setFields(TestHelper.buildManifestVectorJsonPathFieldDTO());

    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    /* Execute Test */

    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(2, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCodes()[0]
            .contains("synkrato.services.partner.product.entitlements.data.json-path.invalid"));

    assertTrue(
        errors
            .getFieldErrors()
            .get(1)
            .getCodes()[0]
            .contains(
                "synkrato.services.partner.product.entitlements.data.scalar-json-path.invalid"));

    assertEquals(
        errors.getFieldErrors().get(1).getArguments()[0],
        "$.applications[0], $.applications[0].borrower.residences[(@.residencyType == 'Current')]");
  }

  // Actor:  Partner Or Application
  // Action: Create P2P Product with webhook
  // Expected Result:  Fail
  @Test
  public void test30ValidateWebhookFailure() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setIntegrationType(IntegrationType.P2P);
    productDTO.setWebhooks(TestHelper.buildWebhookDTO());

    WebhookDTO webhookDTO = new WebhookDTO();
    webhookDTO.setUrl("test.com");
    webhookDTO.setSigningkey("BQ1dOoYdb52Lkjsrhei7PjAf3*gGARdg8S3@fz2UAA1gRn^bKcRU");
    webhookDTO.setEvents(Arrays.asList("created"));
    webhookDTO.setResource("urn:elli:epc:transaction");
    List<WebhookDTO> webhookDTOList = new ArrayList<>();
    webhookDTOList.add(webhookDTO);

    productDTO.setWebhooks(webhookDTOList);
    Errors errors = new BindException(productDTO, "Product");
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Setup mock objects */
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCodes()[0]
            .contains("synkrato.services.partner.product.webhook.not-required"));
  }

  // Actor:  Partner JWT
  // Action: Create Product with extensionLimit out of range
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)

  @Test
  public void test31ValidateExtensionLimitWithDataFailure() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(-1);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.invalid.extensionLimit"));
  }

  // Actor:  Partner JWT
  // Action: Create Product with extensionLimit in range
  // Expected Result:  Fail ( errors.getFieldErrors().size() == 1)

  @Test
  public void test32ValidateExtensionLimitWithDataSuccess() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor:  InternalAdmin JWT
  // Action: Create Product with valid Result
  // Expected Result:  Product created successfully.

  @Test
  public void test33ValidResponseResult() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .forEach(
            transactionEntitlementDTO -> {
              transactionEntitlementDTO.getResponse().setResults(TestHelper.buildResults());
            });
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    when(s3ClientCache.getS3ObjectKeyNames(any(), any()))
        .thenReturn(TestHelper.buildResultDTO("").getFormats());

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Actor:  InternalAdmin JWT
  // Action: Create Product with duplicate action in Result
  // Expected Result:  Product creation failed

  @Test
  public void test34ValidateDuplicateAction() {

    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .forEach(
            transactionEntitlementDTO -> {
              transactionEntitlementDTO.getResponse().setResults(TestHelper.buildResults());
            });
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .getResults()
        .add(TestHelper.buildResultDTO("lock"));

    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    when(s3ClientCache.getS3ObjectKeyNames(any(), any()))
        .thenReturn(TestHelper.buildResultDTO("").getFormats());

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.results.action.duplicate"));
  }

  // Actor:  InternalAdmin JWT
  // Action: Create Product with empty action in Result
  // Expected Result:  Product creation failed

  @Test
  public void test35ValidateEmptyAction() {

    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .forEach(
            transactionEntitlementDTO -> {
              transactionEntitlementDTO.getResponse().setResults(TestHelper.buildResults());
            });
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .getResults()
        .add(TestHelper.buildResultDTO(""));

    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    when(s3ClientCache.getS3ObjectKeyNames(any(), any()))
        .thenReturn(TestHelper.buildResultDTO("").getFormats());

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.results.action.required"));
  }

  // Actor:  InternalAdmin JWT
  // Action: Create Product with null action in Result
  // Expected Result:  Product creation failed

  @Test
  public void test36ValidateNullAction() {

    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .forEach(
            transactionEntitlementDTO -> {
              transactionEntitlementDTO.getResponse().setResults(TestHelper.buildResults());
            });
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .getResults()
        .add(TestHelper.buildResultDTO(null));

    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    when(s3ClientCache.getS3ObjectKeyNames(any(), any()))
        .thenReturn(TestHelper.buildResultDTO("").getFormats());

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.results.action.required"));
  }

  // Actor:  InternalAdmin JWT
  // Action: Create Product with empty format in Result
  // Expected Result:  Product creation failed

  @Test
  public void test37ValidateEmptyFormat() {

    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .forEach(
            transactionEntitlementDTO -> {
              transactionEntitlementDTO.getResponse().setResults(TestHelper.buildResults());
            });
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .getResults()
        .get(0)
        .setFormats(new ArrayList<>());

    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    when(s3ClientCache.getS3ObjectKeyNames(any(), any()))
        .thenReturn(TestHelper.buildResultDTO("").getFormats());

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.results.formats.required"));
  }

  // Actor:  InternalAdmin JWT
  // Action: Create Product with null format in Result
  // Expected Result:  Product creation failed
  @Test
  public void test38ValidateNullFormat() {

    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .forEach(
            transactionEntitlementDTO -> {
              transactionEntitlementDTO.getResponse().setResults(TestHelper.buildResults());
            });
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .getResults()
        .get(0)
        .setFormats(null);

    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    when(s3ClientCache.getS3ObjectKeyNames(any(), any()))
        .thenReturn(TestHelper.buildResultDTO("").getFormats());

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.results.formats.required"));
  }

  // Actor:  InternalAdmin JWT
  // Action: Create Product with duplicate format in Result
  // Expected Result:  Product creation failed
  @Test
  public void test39ValidateDuplicateFormat() {

    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .forEach(
            transactionEntitlementDTO -> {
              transactionEntitlementDTO.getResponse().setResults(TestHelper.buildResults());
            });
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .forEach(
            transactionEntitlementDTO -> {
              transactionEntitlementDTO.getResponse().setResults(TestHelper.buildResults());
            });

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .getResults()
        .get(0)
        .getFormats()
        .add("application/vnd.productpricing-lock-1.0.0+json");

    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    when(s3ClientCache.getS3ObjectKeyNames(any(), any()))
        .thenReturn(TestHelper.buildResultDTO("").getFormats());

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                    "synkrato.services.partner.product.entitlements.data.transactions.resource.attribute.duplicate"));
  }

  // Actor:  InternalAdmin JWT
  // Action: Create Product with format in Result that can bot be found on S3.
  // Expected Result:  Product creation failed

  @Test
  public void test40ValidateNonExistingFormat() {

    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .forEach(
            transactionEntitlementDTO -> {
              transactionEntitlementDTO.getResponse().setResults(TestHelper.buildResults());
            });

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .getResults()
        .get(0)
        .getFormats()
        .add("application/vnd.productpricing-lock-100.0.0+json");

    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);
    when(s3ClientCache.getS3ObjectKeyNames(any(), any()))
        .thenReturn(TestHelper.buildResultDTO("").getFormats());

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.results.formats.notFound"));
  }

  // Actor:  Partner JWT
  // Action: Create Product with empty resources
  // Expected Result:  Product creation failed

  @Test
  public void test41ValidateEmptyResources() {

    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .setResources(new ArrayList<>());

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .setResources(new ArrayList<>());

    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(2, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.results.resources.notEmpty"));

    assertTrue(
        errors
            .getFieldErrors()
            .get(1)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.results.resources.notEmpty"));
  }

  // Actor:  Partner JWT
  // Action: Create Product with duplicate resources types
  // Expected Result:  Product creation failed

  @Test
  public void test42ValidateDuplicateResourcesTypes() {

    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .setResources(new ArrayList<>(Arrays.asList("Invoice", "Invoice")));

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .setResources(new ArrayList<>(Arrays.asList("Invoice", "Invoice")));

    Errors errors = new BindException(productDTO, "Product");
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(2, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                    "synkrato.services.partner.product.entitlements.data.transactions.resource.attribute.duplicate"));

    assertTrue(
        errors
            .getFieldErrors()
            .get(1)
            .getCode()
            .contains(
                    "synkrato.services.partner.product.entitlements.data.transactions.resource.attribute.duplicate"));
  }

  // Action: Create Product request data Entitlement with invalid conditions[] type
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test43ValidateDataEntitlementConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getConditions()
        .get(0)
        .setType("field123");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.entitlements.data.conditions.type.invalid"));
  }

  // Action: Create Product request data Entitlement with valid conditions[] type
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 0)
  @Test
  public void test44ValidateDataEntitlementConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getConditions()
        .get(0)
        .setType("fields");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Action: Create Product request data Entitlement with missing type in conditions[]
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test45ValidateDataEntitlementConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getConditions()
        .get(0)
        .setType(null);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
  }

  // Action: Create Product request data Entitlement with empty type in conditions[]
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test46ValidateDataEntitlementConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getConditions()
        .get(0)
        .setType("");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.attributes.notEmpty"));
  }

  // Action: Create Product request data Entitlement with empty fields in conditions[]
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test47ValidateDataEntitlementConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getConditions()
        .get(0)
        .setRequired(new ArrayList<>());

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.attributes.notEmpty"));
  }

  // Action: Create Product request data Entitlement with null fields in conditions[]
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test48ValidateDataEntitlementConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getConditions()
        .get(0)
        .setRequired(null);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.attributes.required"));
  }

  // Action: Create Product request data Entitlement with empty field in conditions[]
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test49ValidateDataEntitlementConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    List<String> requiredFields = new ArrayList<>();
    requiredFields.add("");

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getConditions()
        .get(0)
        .setRequired(requiredFields);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.attributes.notEmpty"));
  }

  // Action: Create Product request data Entitlement with matching required field in conditions[]
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 0)
  @Test
  public void test50ValidateDataEntitlementConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    List<String> requiredFields = new ArrayList<>();
    requiredFields.add("$.applications[0].borrower.ssn");
    requiredFields.add("$.applications[0].borrower.lastName");

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getConditions()
        .get(0)
        .setRequired(requiredFields);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Action: Create Product request data Entitlement with duplicate required field in conditions[]
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test51ValidateDataEntitlementConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    List<String> requiredFields = new ArrayList<>();
    requiredFields.add("$.applications[0].borrower.ssn");
    requiredFields.add("$.applications[0].borrower.ssn");

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getConditions()
        .get(0)
        .setRequired(requiredFields);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.entitlements.data.conditions.duplicate.found"));
  }

  // Action: Create Product request data Entitlement with incorrect required field in conditions[]
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test52ValidateDataEntitlementConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    List<String> requiredFields = new ArrayList<>();
    requiredFields.add("$.loan.baseAmount");

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .getConditions()
        .get(0)
        .setRequired(requiredFields);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.entitlements.data.conditions.required.fields.not-found"));
  }

  // Action: Create Product request data Entitlement with empty conditions[]
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test53ValidateDataEntitlementConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .setConditions(null);

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .setConditions(new ArrayList<>());

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.attributes.notEmpty"));
  }

  // Action: Create Product response data Entitlement with conditions which is not supported
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test54ValidateDataEntitlementResponseConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .setConditions(TestHelper.buildConditionsDTOList());

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.entitlements.data.conditions.not-supported"));
  }

  // Action: Create Product origin data Entitlement with conditions which is not supported
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test55ValidateDataEntitlementOriginConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    productDTO
        .getEntitlements()
        .getData()
        .getOrigin()
        .setConditions(TestHelper.buildConditionsDTOList());

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.entitlements.data.conditions.not-supported"));
  }

  // Action: Create Product request data Entitlement with duplicate conditions
  // Expected Result:  Pass ( errors.getFieldErrors().size() == 1)
  @Test
  public void test56ValidateDataEntitlementDuplicateConditions() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    List<ConditionDTO> conditions = new ArrayList<>();
    conditions.add(TestHelper.buildConditionsDTOList().get(0));
    conditions.add(TestHelper.buildConditionsDTOList().get(0));

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .setConditions(conditions);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.entitlements.data.conditions.type.duplicate.found"));
  }

  // Action: Create Product request with valid exports.
  // Expected Result:  0 error
  @Test
  public void test57ValidateDataEntitlementTransactionRequestExports() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    TestHelper.buildPartnerJWT();

    List<ExportDTO> exportDTOList = new ArrayList<>();
    ExportDTO exportDTO = new ExportDTO();
    exportDTO.setDocType("urn:elli:document:type:ilad-mismo-mi-v3.4");
    exportDTOList.add(exportDTO);

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .setExports(exportDTOList);
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
    assertTrue(
        productDTO
            .getEntitlements()
            .getData()
            .getTransactions()
            .get(0)
            .getRequest()
            .getExports()
            .get(0)
            .isOverrideResources());
  }

  // Action: Create Product request with empty exports list.
  // Expected Result:  1 error
  @Test
  public void test58ValidateDataEntitlementTransactionRequestExports() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    TestHelper.buildPartnerJWT();

    List<ExportDTO> exportDTOList = new ArrayList<>();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .setExports(exportDTOList);
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.entitlements.data.transactions.request.exports.notEmpty"));
  }

  // Action: Create Product request with exports list with empty and duplicate docType.
  // Expected Result:  1 error
  @Test
  public void test59ValidateDataEntitlementTransactionRequestExports() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");
    TestHelper.buildPartnerJWT();

    List<ExportDTO> exportDTOList = new ArrayList<>();
    ExportDTO exportDTO = new ExportDTO();
    exportDTO.setDocType("urn:elli:document:type:ilad-mismo-mi-v3.4");
    exportDTOList.add(exportDTO);

    ExportDTO exportDTO2 = new ExportDTO();
    exportDTO2.setDocType("urn:elli:document:type:ilad-mismo-mi-v3.4");
    exportDTOList.add(exportDTO2);

    ExportDTO exportDTO3 = new ExportDTO();
    exportDTO3.setDocType("aaa");
    exportDTOList.add(exportDTO3);
    ExportDTO exportDTO4 = new ExportDTO();

    exportDTO4.setDocType("bbb");
    exportDTOList.add(exportDTO4);

    ExportDTO exportDTO5 = new ExportDTO();
    exportDTO5.setDocType("bbb");
    exportDTOList.add(exportDTO5);

    ExportDTO exportDTO7 = new ExportDTO();
    exportDTO7.setDocType("ccc");
    exportDTOList.add(exportDTO7);

    ExportDTO exportDTO8 = new ExportDTO();
    exportDTO8.setDocType("ccc");
    exportDTOList.add(exportDTO8);

    ExportDTO exportDTO9 = new ExportDTO();
    exportDTO9.setDocType("ccc");
    exportDTOList.add(exportDTO9);

    ExportDTO exportDTO10 = new ExportDTO();
    exportDTO10.setDocType("");
    exportDTOList.add(exportDTO10);

    ExportDTO exportDTO11 = new ExportDTO();
    exportDTO11.setDocType("");
    exportDTOList.add(exportDTO11);

    exportDTOList.add(null);
    exportDTOList.add(null);

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .setExports(exportDTOList);
    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);
    /* Assert test results */
    assertEquals(7, errors.getFieldErrors().size());

    assertTrue(
        errors.getFieldErrors().stream()
            .anyMatch(
                fieldError ->
                    fieldError
                        .getCode()
                        .contains("synkrato.services.partner.product.attributes.notEmpty")));
    assertTrue(
        errors.getFieldErrors().stream()
            .anyMatch(
                fieldError ->
                    fieldError
                        .getCode()
                        .contains(
                            "synkrato.services.partner.product.entitlements.data.transactions.request.exports.doc-type.notSupported")));

    assertTrue(
        errors.getFieldErrors().stream()
            .anyMatch(
                fieldError ->
                    fieldError
                        .getCode()
                        .contains(
                            "synkrato.services.partner.product.entitlements.data.transactions.request.exports.doc-type.duplicate")));
  }

  // Action: Create Product request with invalid options schema.
  // input: empty schema
  // Expected Result:  2 error
  @Test
  public void test60ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO.getOptions().get(0).setSchema(new HashMap<>());

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.schema.attributes.missing"));
  }

  // Action: Create Product request with invalid options schema.
  // input: no $schema at root
  // Expected Result:  2 error
  @Test
  public void test61ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO.getOptions().get(0).getSchema().remove(SCHEMA_URI_KEY);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.schema.attributes.missing"));
  }

  // Action: Create Product request with invalid options schema.
  // input: invalid $schema uri at root
  // Expected Result:  2 error
  @Test
  public void test62ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO.getOptions().get(0).getSchema().put(SCHEMA_URI_KEY, "www.schema.com");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.schema.attributes.invalid"));
  }

  // Action: Create Product request with invalid options schema.
  // input: no type at root level
  // Expected Result:  2 error
  @Test
  public void test63ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO.getOptions().get(0).getSchema().remove(TYPE_KEY);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.schema.attributes.missing"));
  }

  // Action: Create Product request with invalid options schema.
  // input: invalid type at root level
  // Expected Result:  2 error
  @Test
  public void test64ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO.getOptions().get(0).getSchema().put(TYPE_KEY, "string");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.schema.attributes.invalid"));
  }

  // Action: Create Product request with invalid options schema.
  // input: no properties at root level
  // Expected Result: 2 error
  @Test
  public void test65ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO.getOptions().get(0).getSchema().remove(PROPERTIES_KEY);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.schema.attributes.missing"));
  }

  // Action: Create Product request with invalid options schema.
  // input: empty properties at root level
  // Expected Result: 2 error
  @Test
  public void test66ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO.getOptions().get(0).getSchema().put(PROPERTIES_KEY, new HashMap<>());

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.schema.properties.missing"));
  }

  // Action: Create Product request with invalid options schema.
  // input: invalid data type of properties at root level
  // Expected Result: 2 error
  @Test
  public void test67ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO.getOptions().get(0).getSchema().put(PROPERTIES_KEY, "reportOn");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.schema.properties.invalid"));
  }

  // Action: Create Product request with valid options schema.
  // input: valid options schema
  // Expected Result: 0 error
  @Test
  public void test68ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  // Action: Create Product request with invalid options schema.
  // input: unsupported datatype inside properties
  // Expected Result: 2 error
  @Test
  public void test69ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    ((Map) (((Map) productDTO.getOptions().get(0).getSchema().get(PROPERTIES_KEY)).get("reportOn")))
        .put("type", "decimal");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.schema.validation.error"));
  }

  // Action: Create Product request with invalid options schema.
  // input: missing datatype inside properties
  // Expected Result: 2 error
  @Test
  public void test70ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    ((Map) (((Map) productDTO.getOptions().get(0).getSchema().get(PROPERTIES_KEY)).get("reportOn")))
        .remove("type");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.schema.validation.error"));
  }

  // Action: Create Product request options schema
  // input: duplicate requestTypes
  // Expected Result: 1 error
  @Test
  public void test71ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO.getOptions().get(0).setRequestTypes(Arrays.asList("NEWREQUEST", "NEWREQUEST"));

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.options.requestTypes.same-requestTypes.duplicate"));
  }

  // Action: Create Product request options schema
  // input: duplicate requestTypes across options
  // Expected Result: 1 error
  @Test
  public void test72ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO.getOptions().get(0).setRequestTypes(Arrays.asList("NEWREQUEST"));
    productDTO.getOptions().get(1).setRequestTypes(Arrays.asList("NEWREQUEST"));

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.requestTypes.duplicate"));
  }

  // Action: Create Product request options schema
  // input: invalid requestTypes
  // Expected Result: 1 error
  @Test
  public void test73ValidateOptions() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO.getOptions().get(0).setRequestTypes(Arrays.asList("INVALID_REQUEST_TYPE"));

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.options.requestTypes.not-found"));
  }

  // Action: Create Product - request DataEntitlements
  // input: duplicate requestTypes
  // Expected Result: 1 error
  @Test
  public void test74ValidateDataEntitlements() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .setRequestTypes(Arrays.asList("NEWREQUEST", "NEWREQUEST"));

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.data-entitlements.requestTypes.same-requestTypes.duplicate"));
  }

  // Action: Create Product request DataEntitlements
  // input: duplicate requestTypes across DataEntitlements
  // Expected Result: 1 error
  @Test
  public void test75ValidateDataEntitlements() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .add(productDTO.getEntitlements().getData().getTransactions().get(0));
    Errors errors = new BindException(productDTO, "Product");

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .setRequestTypes(Arrays.asList("NEWREQUEST"));
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(1)
        .setRequestTypes(Arrays.asList("NEWREQUEST"));

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.data-entitlements.requestTypes.duplicate"));
  }

  // Action: Create Product request DataEntitlements
  // input: invalid requestTypes
  // Expected Result: 1 error
  @Test
  public void test76ValidateDataEntitlements() throws IOException {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setOptions(TestHelper.buildOptionsDTOList());
    Errors errors = new BindException(productDTO, "Product");

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .setRequestTypes(Arrays.asList("INVALID_REQUEST_TYPE"));

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(any())).thenReturn(true);

    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.data-entitlements.requestTypes.not-found"));
  }

  /**
   * test supported doc types
   *
   * @param input input_doc_type from ValueSource
   */
  @ParameterizedTest
  @ValueSource(
      strings = {
        LOAN_DELIVERY_FANNIE_DOC_TYPE,
        FANNIE_DOC_TYPE,
        FREDDIE_DOC_TYPE,
        CLOSING_EXTENDED_DOC_TYPE,
        UCD_DOC_TYPE,
        UCD_FINAL_DOC_TYPE,
        CD33_DOC_TYPE,
        AUS24_DOC_TYPE,
        CLOSING231_LOCK_FORM_DOC_TYPE,
        LE33_DOC_TYPE,
        ULADDU_DOC_TYPE,
        ULADPA_DOC_TYPE,
        ILAD_DOC_TYPE
      })
  void test77CheckUnSupportedDocType(String input) {
    ExportDTO exportDTO = TestHelper.buildExportDTO(input);

    Set<String> errorCodeAndValueSet = new HashSet<>();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    Errors errors = new BindException(productDTO, "Product");

    ReflectionTestUtils.invokeMethod(
        new ProductValidator(), "checkUnSupportedDocType", exportDTO, errorCodeAndValueSet, errors);

    Assert.assertEquals(0, errors.getErrorCount());
  }

  /** test supported doc type for failure case */
  @Test
  public void test78CheckUnSupportedDocTypeFail() {
    ExportDTO exportDTO = TestHelper.buildExportDTO("urn:test:test:format");

    Set<String> errorCodeAndValueSet = new HashSet<>();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    Errors errors = new BindException(productDTO, "Product");

    ReflectionTestUtils.invokeMethod(
        new ProductValidator(), "checkUnSupportedDocType", exportDTO, errorCodeAndValueSet, errors);

    Assert.assertEquals(1, errors.getErrorCount());
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with invalid entitlements.data.findings.codes and statues
   * Expected Result: 2 Errors
   */
  @Test
  public void test79ValidateFindingsWithDuplicatesFailure() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    FindingDTO findingDTO = TestHelper.buildFindingtDTO();
    findingDTO.setTypes(
        Arrays.asList(
            new FindingTypeDTO("INC-101", "finding 101"),
            new FindingTypeDTO("INC-101 ", "duplicate with space "),
            new FindingTypeDTO(" inc-101", "duplicate with space and lower case")));
    productDTO.getEntitlements().getData().setFindings(findingDTO);
    Errors errors = new BindException(productDTO, "Product");

    List<String> statuses = new ArrayList<>();
    statuses.add("Hello"); // one duplicate failure
    statuses.add("hello");
    statuses.add("heLLO");

    productDTO.getEntitlements().getData().getFindings().setStatuses(statuses);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(2, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.duplicate"));
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with invalid entitlements.data.findings.types
   * Expected Result: 2 Errors
   */
  @Test
  public void test80ValidateFindingsWithNullTypesFailure() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.getEntitlements().getData().setFindings(TestHelper.buildFindingtDTO());
    Errors errors = new BindException(productDTO, "Product");

    FindingTypeDTO findingTypeDTO1 = FindingTypeDTO.builder().build();
    FindingTypeDTO findingTypeDTO2 = FindingTypeDTO.builder().build();
    findingTypeDTO2.setCode(null);

    productDTO
        .getEntitlements()
        .getData()
        .getFindings()
        .setTypes(Arrays.asList(findingTypeDTO1, findingTypeDTO2));

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(4, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.notEmpty.with-index"));
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with valid entitlements.data.findings
   * Expected Result: 0 Errors
   */
  @Test
  public void test81ValidateFindingsSuccess() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.getEntitlements().getData().setFindings(TestHelper.buildFindingtDTO());
    Errors errors = new BindException(productDTO, "Product");

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with multiple entitlements.data.findings.statuses empty and check only one error returned
   * Expected Result: 2 Errors
   */
  @Test
  public void test82ValidateFindingsWithNullAndSameStatusesFailure() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.getEntitlements().getData().setFindings(TestHelper.buildFindingtDTO());
    Errors errors = new BindException(productDTO, "Product");

    List<String> statuses = new ArrayList<>();
    statuses.add(null); // null, "", "  " should be one error
    statuses.add("");
    statuses.add("  ");
    statuses.add("*");
    statuses.add("*");

    productDTO.getEntitlements().getData().getFindings().setStatuses(statuses);
    productDTO.getEntitlements().getData().getFindings().setOutboundStatuses(statuses);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(4, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.attributes.notEmpty"));
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with valid feature where sendFindings is true
   * Expected Result: 0 Errors
   */
  @Test
  public void test83ValidateFeatureSuccess() {

    /* Arrange */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    FeatureDTO featureDTO = FeatureDTO.builder().sendFindings(true).build();
    productDTO.setFeature(featureDTO);

    productDTO.getEntitlements().getData().setFindings(TestHelper.buildFindingtDTO());
    Errors errors = new BindException(productDTO, "Product");

    /* Mock */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);

    /* Act */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert */
    assertEquals(0, errors.getFieldErrors().size());
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with valid feature values where sendFindings and receiveAutomatedFindingUpdates is true
   * Expected Result: 0 Errors
   */
  @Test
  public void test84ValidateFeatureSuccess() {

    /* Arrange */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    FeatureDTO featureDTO =
        FeatureDTO.builder().sendFindings(true).receiveAutomatedFindingUpdates(true).build();
    productDTO.setFeature(featureDTO);

    productDTO.getEntitlements().getData().setFindings(TestHelper.buildFindingtDTO());
    Errors errors = new BindException(productDTO, "Product");

    /* Mock */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);

    /* Act */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert */
    assertEquals(0, errors.getFieldErrors().size());
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with invalid feature with sendFinding null and receiveAutomatedFindingUpdates as true
   * Expected Result: 1 Error
   */
  @Test
  public void test85ValidateReceiveAutomatedFindingUpdatesWithoutSendFindingFailure() {

    /* Arrange */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    FeatureDTO featureDTO =
        FeatureDTO.builder().sendFindings(null).receiveAutomatedFindingUpdates(true).build();
    productDTO.setFeature(featureDTO);

    productDTO.getEntitlements().getData().setFindings(TestHelper.buildFindingtDTO());
    Errors errors = new BindException(productDTO, "Product");

    /* Mock */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);

    /* Act */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert */
    assertEquals(1, errors.getFieldErrors().size());
    assertEquals(
        "synkrato.services.partner.product.entitlements.features.invalid.finding.feature",
        errors.getFieldErrors().get(0).getCode());
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with invalid feature with sendFinding not present and receiveAutomatedFindingUpdates as true
   * Expected Result: 1 Error
   */
  @Test
  public void test86ValidateReceiveAutomatedFindingUpdatesWithoutSendFindingFailure() {

    /* Arrange */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    FeatureDTO featureDTO = FeatureDTO.builder().receiveAutomatedFindingUpdates(true).build();
    productDTO.setFeature(featureDTO);

    productDTO.getEntitlements().getData().setFindings(TestHelper.buildFindingtDTO());
    Errors errors = new BindException(productDTO, "Product");

    /* Mock */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);

    /* Act */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert */
    assertEquals(1, errors.getFieldErrors().size());
    assertEquals(
        "synkrato.services.partner.product.entitlements.features.invalid.finding.feature",
        errors.getFieldErrors().get(0).getCode());
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with invalid feature with sendFinding false and receiveAutomatedFindingUpdatess true
   * Expected Result: 1 Error
   */
  @Test
  public void test87ValidateReceiveAutomatedFindingUpdatesWithFalseSendFindingFailure() {

    /* Arrange */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    FeatureDTO featureDTO =
        FeatureDTO.builder().sendFindings(false).receiveAutomatedFindingUpdates(true).build();
    productDTO.setFeature(featureDTO);

    productDTO.getEntitlements().getData().setFindings(TestHelper.buildFindingtDTO());
    Errors errors = new BindException(productDTO, "Product");

    /* Mock */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);

    /* Act */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert */
    assertEquals(1, errors.getFieldErrors().size());
    assertEquals(
        "synkrato.services.partner.product.entitlements.features.invalid.finding.feature",
        errors.getFieldErrors().get(0).getCode());
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with invalid entitlements.data.serviceEvents
   * Expected Result: 2 Errors
   */
  @Test
  public void test89ValidateServiceEventsNullFailures() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    ServiceEventDTO serviceEventDTO = TestHelper.buildServiceEventDTO();
    serviceEventDTO.setTypes(
        Arrays.asList(
            ServiceEventTypeDTO.builder().code(null).build(),
            ServiceEventTypeDTO.builder().code(" ").name(" ").build()));
    productDTO.getEntitlements().getData().setServiceEvents(serviceEventDTO);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(8, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.notEmpty.with-index"));
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with invalid entitlements.data.serviceEvents
   * Expected Result: 3 Errors
   */
  @Test
  public void test90ValidateServiceEventsInvalidFailures() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    ServiceEventDTO serviceEventDTO = TestHelper.buildServiceEventDTO();
    serviceEventDTO.setTypes(
        Arrays.asList(
            ServiceEventTypeDTO.builder()
                .code("99")
                .name("test")
                .senders(Arrays.asList("invalid"))
                .type(Arrays.asList("automated"))
                .build(),
            ServiceEventTypeDTO.builder()
                .code("99")
                .name("test 2")
                .senders(Arrays.asList(" "))
                .type(Arrays.asList("manual"))
                .build()));

    productDTO.getEntitlements().getData().setServiceEvents(serviceEventDTO);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(3, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.entitlements.service-events.sender.invalid.with-index"));
    assertTrue(
        errors
            .getFieldErrors()
            .get(2)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.duplicate"));
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with invalid entitlements.data.serviceEvents
   * Expected Result: Pass
   */
  @Test
  public void test91ValidateServiceEventsSuccess() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    ServiceEventDTO serviceEventDTO = TestHelper.buildServiceEventDTO();
    productDTO.getEntitlements().getData().setServiceEvents(serviceEventDTO);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with invalid entitlements.data.serviceEvents
   * Expected Result: 3 Errors
   */
  @Test
  public void test92ValidateServiceEventsInvalidFailures() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    ServiceEventDTO serviceEventDTO = TestHelper.buildServiceEventDTO();
    serviceEventDTO.setTypes(
        Arrays.asList(
            ServiceEventTypeDTO.builder()
                .code("99")
                .name("test")
                .senders(Arrays.asList("partner"))
                .type(Arrays.asList("invalid"))
                .build(),
            ServiceEventTypeDTO.builder()
                .code("99")
                .name("test 2")
                .senders(Arrays.asList("lender"))
                .type(Arrays.asList(" "))
                .build()));

    productDTO.getEntitlements().getData().setServiceEvents(serviceEventDTO);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(3, errors.getFieldErrors().size());
    assertTrue(
        errors
            .getFieldErrors()
            .get(0)
            .getCode()
            .contains(
                "synkrato.services.partner.product.entitlements.service-events.type.invalid.with-index"));
    assertTrue(
        errors
            .getFieldErrors()
            .get(2)
            .getCode()
            .contains("synkrato.services.partner.product.entitlements.duplicate"));
  }

  /*
   * Actor: InternalAdmin JWT
   * Action: Create Product with invalid entitlements.data.serviceEvents
   * Expected Result: Pass
   */
  @Test
  public void test93ValidateServiceEventsSuccess() {
    /* Build Test Data */
    TestHelper.buildInternalAdminJwt();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Errors errors = new BindException(productDTO, "Product");

    ServiceEventDTO serviceEventDTO = TestHelper.buildServiceEventDTO();
    productDTO.getEntitlements().getData().setServiceEvents(serviceEventDTO);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(0, errors.getFieldErrors().size());
  }

  /*
   * Actor: partner JWT
   * Action: Create Product with duplicate additionalLink.type
   * Expected Result: 1 error
   */
  @Test
  public void test94ValidateAdditionalLinkedFailure() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getAdditionalLinks()
        .add(AdditionalLinkDTO.builder().type(AdditionalLinkType.PRODUCT_CONFIG_URL).build());
    Errors errors = new BindException(productDTO, "Product");

    ServiceEventDTO serviceEventDTO = TestHelper.buildServiceEventDTO();
    productDTO.getEntitlements().getData().setServiceEvents(serviceEventDTO);

    /* Setup mock objects */
    when(productUtil.isValidDataType(any(), any())).thenReturn(true);
    when(productUtil.hasDataEntitlements(productDTO.getEntitlements())).thenReturn(true);

    productDTO.setExtensionLimit(11);
    /* Execute Test */
    ValidationUtils.invokeValidator(productValidator, productDTO, errors);

    /* Assert test results */
    assertEquals(1, errors.getFieldErrors().size());
    assertEquals(
        "synkrato.services.partner.product.additional-link.type.duplicate",
        errors.getFieldErrors().get(0).getCode());
  }
}
