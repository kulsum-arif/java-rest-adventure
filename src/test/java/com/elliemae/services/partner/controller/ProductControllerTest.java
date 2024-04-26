package com.synkrato.services.partner.controller;

import static com.synkrato.services.partner.controller.BaseController.PRODUCT_CONTEXT;
import static com.synkrato.services.partner.controller.BaseController.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.synkrato.services.epc.common.dto.ExportDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.marshaller.EpcMarshaller;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.ProductService;
import com.synkrato.services.partner.data.Product;
import com.synkrato.services.partner.dto.ProductListQueryDTO;
import com.synkrato.services.partner.dto.ProductQueryDTO;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import com.synkrato.services.partner.enums.EntityView;
import com.synkrato.services.partner.util.ProductUtil;
import com.synkrato.services.partner.util.TestHelper;
import com.synkrato.services.partner.validators.ProductSearchValidator;
import com.synkrato.services.partner.validators.ProductValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

@RunWith(MockitoJUnitRunner.class)
public class ProductControllerTest {

  @InjectMocks ProductController productController;
  MockHttpServletRequest request;
  @Mock MockHttpServletResponse response;
  EpcMarshaller<ProductSearchDTO> productSearchMarshaller;
  @Mock ProductService productService;
  @Mock ProductValidator productValidator;
  @Mock MessageUtil messageUtil;
  ProductUtil productUtil;
  ProductSearchValidator productSearchValidator;

  ProductSearchDTO productSearch = null;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("localhost");
    request.setServerPort(8080);
    request.setContextPath(VERSION + PRODUCT_CONTEXT);

    ProductSearchDTO productSearch =
        ProductSearchDTO.builder()
            .partnerId(TestHelper.TEST_PARTNER_ID)
            .name(TestHelper.TEST_PRODUCT_NAME)
            .build();
    productUtil = new ProductUtil();
    productSearchValidator = new ProductSearchValidator();
    ObjectMapper objectMapper = new ObjectMapper();
    productSearchMarshaller = new EpcMarshaller<>();
    ReflectionTestUtils.setField(productSearchMarshaller, "objectMapper", objectMapper);
    ReflectionTestUtils.setField(productSearchValidator, "messageUtil", messageUtil);
    ReflectionTestUtils.setField(
        productController, "productSearchMarshaller", productSearchMarshaller);
    ReflectionTestUtils.setField(productController, "productUtil", productUtil);
    ReflectionTestUtils.setField(
        productController, "productSearchValidator", productSearchValidator);
    ReflectionTestUtils.setField(productController, "productValidator", productValidator);
    ReflectionTestUtils.setField(productController, "objectMapper", objectMapper);
  }

  @Test
  public void test1createProduct() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    ProductQueryDTO productQueryDTO = ProductQueryDTO.builder().view(EntityView.ID.name()).build();

    /* Setup mock objects */
    when(productService.create(productDTO, productQueryDTO.getView())).thenReturn(productDTO);

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.createProduct(productDTO, productQueryDTO, request);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
  }

  @Test
  public void test1createProductPOST() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    request.setMethod("POST");
    ProductQueryDTO productQueryDTO = ProductQueryDTO.builder().view(EntityView.ID.name()).build();

    /* Setup mock objects */
    when(productService.create(productDTO, productQueryDTO.getView())).thenReturn(productDTO);

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.createProduct(productDTO, productQueryDTO, request);

    /* Assert test results */

    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
  }

  @Test
  public void test1findAll() throws Exception {

    /* Build Test Data */

    TestHelper.buildPartnerJWT();
    Map<String, Object> productSearchMap = TestHelper.buildProductSearchMap();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    List<ProductDTO> productList = new ArrayList<>();
    productList.add(productDTO);

    ProductListQueryDTO productQueryDTO =
        ProductListQueryDTO.builder().view(EntityView.DEFAULT.name()).build();

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findAll(productSearchMap, productQueryDTO, response);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findById() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    ProductQueryDTO productQueryDTO =
        ProductQueryDTO.builder().view(EntityView.INTEGRATIONTYPE.name()).build();

    /* Setup mock objects */
    when(productService.findById(productDTO.getPartnerId(), productQueryDTO.getView()))
        .thenReturn(productDTO);

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findById(productDTO.getPartnerId(), productQueryDTO);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findAllViewId() throws Exception {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productSearchMap = TestHelper.buildProductSearchMap();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    List<ProductDTO> productList = new ArrayList<>();
    productList.add(productDTO);

    ProductListQueryDTO productQueryDTO =
        ProductListQueryDTO.builder().view(EntityView.DEFAULT.name()).build();

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findAll(productSearchMap, productQueryDTO, response);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findAllViewIntegrationtype() throws Exception {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productSearchMap = TestHelper.buildProductSearchMap();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    List<ProductDTO> productList = new ArrayList<>();
    productList.add(productDTO);

    ProductListQueryDTO productQueryDTO =
        ProductListQueryDTO.builder().view(EntityView.INTEGRATIONTYPE.name()).build();

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findAll(productSearchMap, productQueryDTO, response);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findAllViewEntitlement() throws Exception {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productSearchMap = TestHelper.buildProductSearchMap();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    List<ProductDTO> productList = new ArrayList<>();
    productList.add(productDTO);

    String view = "ENTITLEMENT";

    ProductListQueryDTO productQueryDTO =
        ProductListQueryDTO.builder().view(EntityView.DEFAULT.name()).build();

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findAll(productSearchMap, productQueryDTO, response);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findAllViewWebhook() throws Exception {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productSearchMap = TestHelper.buildProductSearchMap();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    List<ProductDTO> productList = new ArrayList<>();
    productList.add(productDTO);

    ProductListQueryDTO productQueryDTO =
        ProductListQueryDTO.builder().view(EntityView.DEFAULT.name()).build();

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findAll(productSearchMap, productQueryDTO, response);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findAllViewComplete() throws Exception {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    Map<String, Object> productSearchMap = TestHelper.buildProductSearchMap();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    List<ProductDTO> productList = new ArrayList<>();
    productList.add(productDTO);

    ProductListQueryDTO productQueryDTO =
        ProductListQueryDTO.builder().view(EntityView.DEFAULT.name()).build();

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findAll(productSearchMap, productQueryDTO, response);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findByIdViewId() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    ProductQueryDTO productQueryDTO = ProductQueryDTO.builder().view(EntityView.ID.name()).build();

    /* Setup mock objects */
    when(productService.findById(productDTO.getPartnerId(), productQueryDTO.getView()))
        .thenReturn(productDTO);

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findById(productDTO.getPartnerId(), productQueryDTO);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findByIdViewIntegrationtype() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    ProductQueryDTO productQueryDTO =
        ProductQueryDTO.builder().view(EntityView.INTEGRATIONTYPE.name()).build();

    /* Setup mock objects */
    when(productService.findById(productDTO.getPartnerId(), productQueryDTO.getView()))
        .thenReturn(productDTO);

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findById(productDTO.getPartnerId(), productQueryDTO);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findByIdViewEntitlement() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    ProductQueryDTO productQueryDTO =
        ProductQueryDTO.builder().view(EntityView.ENTITLEMENT.name()).build();

    /* Setup mock objects */
    when(productService.findById(productDTO.getPartnerId(), productQueryDTO.getView()))
        .thenReturn(productDTO);

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findById(productDTO.getPartnerId(), productQueryDTO);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findByIdViewWebhook() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    ProductQueryDTO productQueryDTO =
        ProductQueryDTO.builder().view(EntityView.WEBHOOK.name()).build();

    /* Setup mock objects */
    when(productService.findById(productDTO.getPartnerId(), productQueryDTO.getView()))
        .thenReturn(productDTO);

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findById(productDTO.getPartnerId(), productQueryDTO);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findByIdViewComplete() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    ProductQueryDTO productQueryDTO =
        ProductQueryDTO.builder().view(EntityView.COMPLETE.name()).build();

    /* Setup mock objects */
    when(productService.findById(productDTO.getPartnerId(), productQueryDTO.getView()))
        .thenReturn(productDTO);

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findById(productDTO.getPartnerId(), productQueryDTO);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  public void test1findByIdViewExports() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();

    List<ExportDTO> exportDTOList = new ArrayList<>();
    exportDTOList.add(ExportDTO.builder().docType("123").build());
    exportDTOList.add(ExportDTO.builder().docType("abc").build());

    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getRequest()
        .setExports(exportDTOList);

    ProductQueryDTO productQueryDTO =
        ProductQueryDTO.builder().view(EntityView.EXPORTS.name()).build();

    /* Setup mock objects */
    when(productService.findById(productDTO.getPartnerId(), productQueryDTO.getView()))
        .thenReturn(productDTO);

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findById(productDTO.getPartnerId(), productQueryDTO);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getBody());
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  /*
   * This method is to test the update product API (PATCH)
   */
  @Test
  public void test1UpdateProductTest() throws Exception {
    // Arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Map<String, Object> productUpdateMap = TestHelper.buildAsyncProductMap();

    BindingResult bindingResults =
        new BindException(productUpdateMap, Product.class.getSimpleName());

    ProductQueryDTO productQueryDTO = ProductQueryDTO.builder().view(EntityView.ID.name()).build();

    // Mock
    when(productService.update(productDTO.getId(), productUpdateMap, productQueryDTO.getView()))
        .thenReturn(productDTO);

    // Act
    ResponseEntity<Object> response =
        productController.updateProduct(productDTO.getId(), productUpdateMap, productQueryDTO);

    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  /*
   * This method is to get product findings
   */
  @Test
  public void test1findByIdViewFindings() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.getEntitlements().getData().setFindings(TestHelper.buildFindingtDTO());

    ProductQueryDTO productQueryDTO =
        ProductQueryDTO.builder().view(EntityView.FINDINGS.name()).build();

    /* Setup mock objects */
    when(productService.findById(productDTO.getPartnerId(), productQueryDTO.getView()))
        .thenReturn(productDTO);

    /* Execute Test */
    ResponseEntity<Object> responseEntity =
        productController.findById(productDTO.getPartnerId(), productQueryDTO);

    /* Assert test results */
    assertNotNull(responseEntity);
    assertTrue(
        responseEntity.getBody().toString().contains(EntityView.FINDINGS.toString().toLowerCase()));
  }
}
