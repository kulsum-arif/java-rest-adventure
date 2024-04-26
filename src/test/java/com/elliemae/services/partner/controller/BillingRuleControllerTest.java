package com.synkrato.services.partner.controller;

import static com.synkrato.services.partner.controller.BaseController.PRODUCT_CONTEXT;
import static com.synkrato.services.partner.controller.BaseController.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.synkrato.components.microservice.exception.ParameterDataInvalidException;
import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.enums.BillingRuleStatus;
import com.synkrato.services.epc.common.util.JsonSchemaValidationUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.BillingRuleService;
import com.synkrato.services.partner.dto.ProductQueryDTO;
import com.synkrato.services.partner.enums.EntityView;
import com.synkrato.services.partner.util.BillingRuleUtil;
import com.synkrato.services.partner.util.ProductUtil;
import com.synkrato.services.partner.util.TestHelper;
import com.synkrato.services.partner.validators.BillingRuleValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RunWith(MockitoJUnitRunner.class)
public class BillingRuleControllerTest {
  @InjectMocks BillingController billingController;
  MockHttpServletRequest request;
  @Mock MockHttpServletResponse response;

  @Mock BillingRuleService billingRuleService;
  @Mock BillingRuleValidator billingRuleValidator;
  @Mock MessageUtil messageUtil;
  @Mock BillingRuleUtil billingRuleUtil;
  @Mock private ProductUtil productUtil;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("localhost");
    request.setServerPort(8080);
    request.setContextPath(VERSION + PRODUCT_CONTEXT);

    billingRuleUtil = new BillingRuleUtil();
    ObjectMapper objectMapper = new ObjectMapper();

    ReflectionTestUtils.setField(billingController, "billingRuleValidator", billingRuleValidator);
    ReflectionTestUtils.setField(billingController, "objectMapper", objectMapper);
    ReflectionTestUtils.setField(
        billingRuleValidator, "jsonSchemaValidationUtil", new JsonSchemaValidationUtil());
    ReflectionTestUtils.setField(billingRuleValidator, "bucketName", "test-bucket");
    ReflectionTestUtils.setField(billingRuleValidator, "prefix", "test");
  }

  /** This is a positive test case for creating billing rule with complete view */
  @Test
  public void createBillingRuleCompleteViewTest() {

    /* Arrange */
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    BillingRuleDTO billingRuleResult = TestHelper.buildBillingRuleDTO();
    ProductQueryDTO productQueryDTO =
        ProductQueryDTO.builder().view(EntityView.COMPLETE.name()).build();

    String productId = UUID.randomUUID().toString();

    billingRuleDTO.setProductId(productId);
    billingRuleResult.setStatus(BillingRuleStatus.DRAFT);
    billingRuleResult.setProductId(productId);

    /* Mock */
    when(billingRuleService.create(billingRuleDTO)).thenReturn(billingRuleResult);
    doNothing().when(productUtil).buildProductQueryView(any(), anyString());

    /* Act */
    ResponseEntity<Object> responseEntity =
        billingController.createBillingRule(productId, billingRuleDTO, productQueryDTO, request);

    /* Assert  */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    JSONObject response = new JSONObject(responseEntity.getBody().toString());
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    assertNotNull(productId, response.get("productId"));
  }

  /** This is a positive test case for creating billing rule with default view */
  @Test
  public void createBillingRuleDefaultViewTest() {

    /* Arrange */
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    BillingRuleDTO billingRuleResult = TestHelper.buildBillingRuleDTO();
    ProductQueryDTO productQueryDTO =
        ProductQueryDTO.builder().view(EntityView.DEFAULT.name()).build();
    String productId = UUID.randomUUID().toString();

    billingRuleDTO.setProductId(productId);
    billingRuleResult.setStatus(BillingRuleStatus.DRAFT);
    billingRuleResult.setProductId(productId);

    /* Mock */
    when(billingRuleService.create(billingRuleDTO)).thenReturn(billingRuleResult);
    doNothing().when(productUtil).buildProductQueryView(any(), anyString());

    /* Act */
    ResponseEntity<Object> responseEntity =
        billingController.createBillingRule(productId, billingRuleDTO, productQueryDTO, request);

    /* Assert  */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    JSONObject response = new JSONObject(responseEntity.getBody().toString());
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    assertNotNull(productId, response.get("productId"));
  }

  /** This is a positive test case for creating billing rule with default view */
  @Test
  public void createBillingRuleIdViewTest() {

    /* Arrange */
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    BillingRuleDTO billingRuleResult = TestHelper.buildBillingRuleDTO();

    String billingRuleId = billingRuleResult.getId();
    ProductQueryDTO productQueryDTO = ProductQueryDTO.builder().view(EntityView.ID.name()).build();
    String productId = UUID.randomUUID().toString();

    billingRuleDTO.setProductId(productId);
    billingRuleResult.setStatus(BillingRuleStatus.DRAFT);
    billingRuleResult.setProductId(productId);

    /* Mock */
    when(billingRuleService.create(billingRuleDTO)).thenReturn(billingRuleResult);
    doNothing().when(productUtil).buildProductQueryView(any(), anyString());

    /* Act */
    ResponseEntity<Object> responseEntity =
        billingController.createBillingRule(productId, billingRuleDTO, productQueryDTO, request);

    /* Assert  */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    JSONObject response = new JSONObject(responseEntity.getBody().toString());
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    assertNotNull(billingRuleId, response.get("id"));
  }

  /** This is a negative test case for creating billing rule with invalid view */
  @Test(expected = ParameterDataInvalidException.class)
  public void createBillingRuleInvalidViewTest() {

    /* Arrange */
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    BillingRuleDTO billingRuleResult = TestHelper.buildBillingRuleDTO();
    ProductQueryDTO productQueryDTO = ProductQueryDTO.builder().view("invalid").build();

    String productId = UUID.randomUUID().toString();

    billingRuleDTO.setProductId(productId);
    billingRuleResult.setStatus(BillingRuleStatus.DRAFT);
    billingRuleResult.setProductId(productId);

    /* Mock */
    when(billingRuleService.create(billingRuleDTO)).thenReturn(billingRuleResult);
    doNothing().when(productUtil).buildProductQueryView(any(), anyString());

    /* Act */
    ResponseEntity<Object> responseEntity =
        billingController.createBillingRule(productId, billingRuleDTO, productQueryDTO, request);
  }

  /** This is a positive test case for creating billing rule with default view */
  @Test
  public void updateBillingRuleIdViewTest() throws MethodArgumentNotValidException {

    /* Arrange */
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    BillingRuleDTO billingRuleResult = TestHelper.buildBillingRuleDTO();

    String billingRuleId = billingRuleResult.getId();
    ProductQueryDTO productQueryDTO = ProductQueryDTO.builder().view(EntityView.ID.name()).build();
    String productId = UUID.randomUUID().toString();

    billingRuleDTO.setProductId(productId);
    billingRuleResult.setStatus(BillingRuleStatus.DRAFT);
    billingRuleResult.setProductId(productId);
    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});

    /* Mock */
    when(billingRuleService.update(productId, billingRuleId, billingRuleMap))
        .thenReturn(billingRuleResult);
    doNothing().when(productUtil).buildProductQueryView(any(), anyString());

    /* Act */
    ResponseEntity<Object> responseEntity =
        billingController.updateBillingRule(
            productId, billingRuleId, billingRuleMap, productQueryDTO);

    /* Assert  */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    JSONObject response = new JSONObject(responseEntity.getBody().toString());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertNotNull(billingRuleId, response.get("id"));
  }

  /** This is a positive test case for creating billing rule with approved status */
  @Test
  public void updateBillingRuleWithApprovedStatusTest() throws MethodArgumentNotValidException {

    /* Arrange */
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    BillingRuleDTO billingRuleResult = TestHelper.buildBillingRuleDTO();
    billingRuleResult.setStatus(BillingRuleStatus.APPROVED);

    String billingRuleId = billingRuleResult.getId();
    ProductQueryDTO productQueryDTO =
        ProductQueryDTO.builder().view(EntityView.COMPLETE.name()).build();
    String productId = UUID.randomUUID().toString();

    billingRuleDTO.setProductId(productId);
    billingRuleResult.setStatus(BillingRuleStatus.APPROVED);
    billingRuleResult.setProductId(productId);
    Map<String, Object> billingRuleMap =
        new ObjectMapper()
            .convertValue(billingRuleDTO, new TypeReference<Map<String, Object>>() {});

    /* Mock */
    when(billingRuleService.update(productId, billingRuleId, billingRuleMap))
        .thenReturn(billingRuleResult);
    doNothing().when(productUtil).buildProductQueryView(any(), anyString());

    /* Act */
    ResponseEntity<Object> responseEntity =
        billingController.updateBillingRule(
            productId, billingRuleId, billingRuleMap, productQueryDTO);

    /* Assert  */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    JSONObject response = new JSONObject(responseEntity.getBody().toString());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(BillingRuleStatus.APPROVED.getValue(), response.get("status"));
  }
}
