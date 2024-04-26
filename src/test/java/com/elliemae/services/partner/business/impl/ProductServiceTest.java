package com.synkrato.services.partner.business.impl;

import static com.synkrato.services.epc.common.EpcCommonConstants.DML_INSERT;
import static com.synkrato.services.epc.common.EpcCommonConstants.ID_VIEW;
import static com.synkrato.services.epc.common.EpcCommonConstants.WEBHOOK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.synkrato.services.epc.common.dto.FeatureDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.WebhookDTO;
import com.synkrato.services.epc.common.dto.enums.TagType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.kafka.KafkaUtil;
import com.synkrato.services.epc.common.kafka.ProductKafkaEvent.Payload;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.webhook.SubscriptionService;
import com.synkrato.services.partner.data.Product;
import com.synkrato.services.partner.data.jpa.CustomProductRepository;
import com.synkrato.services.partner.data.jpa.ProductRepository;
import com.synkrato.services.partner.data.jpa.domain.ProductPageRequest;
import com.synkrato.services.partner.data.jpa.domain.ProductSpecification;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import com.synkrato.services.partner.util.ProductUpdateUtil;
import com.synkrato.services.partner.util.ProductUtil;
import com.synkrato.services.partner.util.TestHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProductServiceTest {

  private static final String TAG_ATTRIBUTE = "tags";
  @Mock ProductRepository productRepository;
  @Mock CustomProductRepository customProductRepository;
  @Mock ProductSpecification productSpecification;
  @Mock ProductUtil productUtil;
  @Mock ProductUpdateUtil productUpdateUtil;
  @Mock SubscriptionService subscriptionService;
  @Mock MessageUtil messageUtil;
  @Mock KafkaUtil kafkaUtil;

  @InjectMocks ProductServiceImpl productService;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test1CreateProductValid() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    String view = "default";

    /* Setup mock objects */
    when(productUtil.buildProduct(any(), any(), any())).thenReturn(product);
    when(productRepository.save(any())).thenReturn(product);
    when(productUtil.buildProductDTO(product, productDTO, false, view)).thenReturn(productDTO);

    /* Execute Test */
    ProductDTO newProductDTO = productService.create(productDTO, view);

    /* Assert test results */
    assertEquals(productDTO, newProductDTO);
    assertEquals(
        0,
        newProductDTO.getCredentials().stream()
            .filter(prop -> Objects.isNull(prop.getScope()))
            .count());
    /* All the categories while creating should be in upper case */
    assertEquals(
        newProductDTO.getTags().get(TagType.CATEGORIES).stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList()),
        newProductDTO.getTags().get(TagType.CATEGORIES));
  }

  @Test(expected = NullPointerException.class)
  public void test2CreateProductException() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    List<WebhookDTO> subscriptionList = TestHelper.buildSubscriptionList();
    String view = "default";
    Product productEntity = TestHelper.buildProduct(productDTO);

    /* Execute Test */
    productService.create(productDTO, view);
  }

  @Test
  @Ignore
  public void test3FindAllProductValid() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();

    ProductSearchDTO searchAttribute =
        ProductSearchDTO.builder()
            .partnerId(TestHelper.TEST_PARTNER_ID)
            .name(TestHelper.TEST_PRODUCT_NAME)
            .build();

    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);

    List<Product> productList = new ArrayList<>();
    productList.add(product);
    Page<Product> result = new PageImpl(productList);

    String view = "default";

    /* Setup mock objects */
    when(productUtil.buildProductDTO(product, new ProductDTO(), true, view)).thenReturn(productDTO);
    when(customProductRepository.findAll(
            any(ProductSpecification.class), any(ProductPageRequest.class)))
        .thenReturn(result);
    when(productUtil.isAuthorized(product, false)).thenReturn(true);

    /* Execute Test */
    List<ProductDTO> productDTOList = productService.findAll(searchAttribute, view);

    /* Assert test results */
    assertNotNull(productDTOList);
    assertNotNull(productDTOList.get(0));
    assertEquals(productDTOList.get(0).getId(), productDTO.getId());
  }

  @Test
  public void test4findByIdProductValid() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);

    String view = "default";

    /* Setup mock objects */
    when(productRepository.findById(CommonUtil.getUUIDFromString(productDTO.getId())))
        .thenReturn(Optional.of(product));
    when(productUtil.buildProductDTO(product, new ProductDTO(), true, view)).thenReturn(productDTO);

    when(productUtil.isAuthorized(product, false)).thenReturn(true);

    /* Execute Test */
    ProductDTO productDTO1 = productService.findById(productDTO.getId(), view);

    /* Assert test results */
    assertNotNull(productDTO1);
    assertEquals(productDTO1.getId(), productDTO.getId());
  }

  @Test
  public void test5findByIdProductException() {

    /* Build Test Data */
    String partnerId = TestHelper.TEST_PARTNER_ID;

    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);

    String view = "default";

    /* Execute Test */
    ProductDTO productDTO1 = null;
    boolean isException = false;
    try {
      productDTO1 = productService.findById(partnerId, view);

    } catch (Exception ex) {
      isException = true;
    }

    /* Assert test results */
    assertTrue(isException);
  }

  /** Find by partner id and partner name Expected Result: Valid product returned */
  @Test
  public void test8findByPartnerIdAndNameValid() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);

    List<Product> productList = new ArrayList<>();
    productList.add(product);

    /* Setup mock objects */
    when(productRepository.findByPartnerIdAndName(anyString(), anyString()))
        .thenReturn(productList);
    when(productUtil.buildProductDTO(product, new ProductDTO(), true, ID_VIEW))
        .thenReturn(productDTO);

    /* Execute Test */
    List<ProductDTO> productDTOList =
        productService.findByPartnerIdAndName(
            TestHelper.TEST_PARTNER_ID, TestHelper.TEST_PRODUCT_NAME);

    /* Assert test results */
    assertNotNull(productDTOList);
    assertEquals(productDTOList.get(0).getId(), productDTO.getId());
  }

  @Test
  public void test9ValidateRegexValid() {

    String input = ".*";

    when(productRepository.validatePosixRegex(input)).thenReturn(true);

    boolean result = productService.validateRegex(".*");

    assertTrue(result);
  }

  @Test
  public void test9ValidateRegexInvalid() {

    String input = "*";

    when(productRepository.validatePosixRegex(input)).thenReturn(false);

    boolean result = productService.validateRegex("*");

    assertFalse(result);
  }

  /** Update product without valid partner jwt */
  @Test
  public void test10UpdateProduct() throws Exception {
    boolean result = false;

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    Map<String, Object> updateProductMap = TestHelper.buildAsyncProductMap();
    String view = "default";

    /* Setup mock objects */
    when(productRepository.findById(any())).thenReturn(Optional.of(product));

    /* Execute Test */
    try {
      productService.update(productDTO.getId(), updateProductMap, view);
    } catch (EpcRuntimeException ex) {
      if ((ex).getHttpStatus() == HttpStatus.FORBIDDEN) {
        result = true;
      }
    }
    /* Assert test results */
    assertTrue(result);
  }

  /** Update product */
  @Test
  public void test11UpdateProductSuccess() throws Exception {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    Map<String, Object> updateProductMap = TestHelper.buildAsyncProductMap();
    updateProductMap.remove(WEBHOOK);
    List<Map<String, Object>> completePayload = TestHelper.completePayload();
    List<WebhookDTO> subscriptionList = TestHelper.buildSubscriptionList();
    String view = "default";

    /* Setup mock objects */
    when(productUtil.buildProduct(any(), any(), anyString())).thenReturn(product);
    when(productRepository.findById(any())).thenReturn(Optional.of(product));
    when(productUtil.isAuthorized(product, true)).thenReturn(true);
    when(productRepository.saveAndFlush(product)).thenReturn(product);
    when(productUtil.buildProductDTO(any(), any(), anyBoolean(), anyString()))
        .thenReturn(productDTO);
    when(productUpdateUtil.merge(any(), any())).thenReturn(productDTO);
    doNothing().when(productUpdateUtil).premergeValidate(updateProductMap, productDTO);
    doNothing().when(productUpdateUtil).postmergeValidate(productDTO);

    /* Execute Test */
    ProductDTO newProductDTO = productService.update(productDTO.getId(), updateProductMap, view);

    /* Assert test results */
    assertNotNull(newProductDTO);
    assertEquals(newProductDTO.getId(), productDTO.getId());
    assertEquals(newProductDTO.getRequestTypes().size(), productDTO.getRequestTypes().size());
    assertEquals(
        0,
        newProductDTO.getCredentials().stream()
            .filter(prop -> Objects.isNull(prop.getScope()))
            .count());
  }

  /** test while Update product the tags should not get affected if not in the update request */
  @Test
  public void test10UpdateProduct_NoTags() throws Exception {
    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    Map<String, Object> updateProductMap = TestHelper.buildAsyncProductMap();
    updateProductMap.remove(TAG_ATTRIBUTE);
    updateProductMap.remove(WEBHOOK);
    String view = "default";

    /* Setup mock objects */
    when(productUtil.buildProduct(any(), any(), anyString())).thenReturn(product);
    when(productRepository.findById(any())).thenReturn(Optional.of(product));
    when(productUtil.isAuthorized(product, true)).thenReturn(true);
    when(productRepository.saveAndFlush(product)).thenReturn(product);
    when(productUtil.buildProductDTO(any(), any(), anyBoolean(), anyString()))
        .thenReturn(productDTO);
    when(productUpdateUtil.merge(any(), any())).thenReturn(productDTO);
    doNothing().when(productUpdateUtil).premergeValidate(updateProductMap, productDTO);
    doNothing().when(productUpdateUtil).postmergeValidate(productDTO);

    /* Execute Test */
    ProductDTO newProductDTO = productService.update(productDTO.getId(), updateProductMap, view);

    /* Assert test results */
    assertNotNull(newProductDTO);
    assertEquals(newProductDTO.getId(), productDTO.getId());
    assertEquals(
        newProductDTO.getTags().get(TagType.CATEGORIES),
        productDTO.getTags().get(TagType.CATEGORIES));
  }

  /** test Update product with blank tags */
  @Test
  public void test10UpdateProduct_BlankTags() throws Exception {
    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    Map<String, Object> updateProductMap = TestHelper.buildAsyncProductMap();
    updateProductMap.put(TAG_ATTRIBUTE, "");
    updateProductMap.remove(WEBHOOK);
    String view = "default";

    /* Setup mock objects */
    when(productUtil.buildProduct(any(), any(), anyString())).thenReturn(product);
    when(productRepository.findById(any())).thenReturn(Optional.of(product));
    when(productUtil.isAuthorized(product, true)).thenReturn(true);
    when(productRepository.saveAndFlush(product)).thenReturn(product);
    when(productUtil.buildProductDTO(any(), any(), anyBoolean(), anyString()))
        .thenReturn(productDTO);
    when(productUpdateUtil.merge(any(), any())).thenReturn(productDTO);
    doNothing().when(productUpdateUtil).premergeValidate(updateProductMap, productDTO);
    doNothing().when(productUpdateUtil).postmergeValidate(productDTO);

    /* Execute Test */
    ProductDTO newProductDTO = productService.update(productDTO.getId(), updateProductMap, view);

    /* Assert test results */
    assertNotNull(newProductDTO);
    assertEquals(newProductDTO.getId(), productDTO.getId());
  }

  /** test Update product with blank tags */
  @Test
  public void test11PublishKafkaEvent() {
    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    Payload payload = TestHelper.buildKafkaProductPayload(product);

    /* Setup mock objects */
    when(productUtil.buildKafkaProductPayload(any())).thenReturn(payload);
    doNothing().when(kafkaUtil).sendEvent(any(), any(), any());

    /* Execute Test */
    ReflectionTestUtils.invokeMethod(productService, "publishKafkaEvent", product, DML_INSERT);

    /* Assert test results */
    verify(kafkaUtil, times(1)).sendEvent(any(), any(), any());
  }

  /** This is a test to create a product with sendServiceEvents feature flag */
  @Test
  public void testCreateProductWithServiceEventsFeature() {

    // Arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);

    FeatureDTO featureDTO = TestHelper.buildFeatureDTO();
    featureDTO.setSendServiceEvents(true);

    productDTO.setFeature(TestHelper.buildFeatureDTO());
    String view = "default";

    // Mock
    when(productUtil.buildProduct(any(), any(), any())).thenReturn(product);
    when(productRepository.save(any())).thenReturn(product);
    when(productUtil.buildProductDTO(product, productDTO, false, view)).thenReturn(productDTO);

    // Act
    ProductDTO newProductDTO = productService.create(productDTO, view);

    // Assert
    assertEquals(productDTO, newProductDTO);
    assertEquals(
        0,
        newProductDTO.getCredentials().stream()
            .filter(prop -> Objects.isNull(prop.getScope()))
            .count());
    /* All the categories while creating should be in upper case */
    assertEquals(
        newProductDTO.getTags().get(TagType.CATEGORIES).stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList()),
        newProductDTO.getTags().get(TagType.CATEGORIES));
    assertEquals(
        newProductDTO.getFeature().getSendServiceEvents(),
        productDTO.getFeature().getSendServiceEvents());
  }

  /** This is a test to create a product with sendServiceEvents feature flag */
  @Test
  public void testCreateProductWithServiceEventsFeatureNull() {

    // Arrange
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);

    FeatureDTO featureDTO = TestHelper.buildFeatureDTO();
    featureDTO.setSendServiceEvents(null);

    productDTO.setFeature(TestHelper.buildFeatureDTO());
    String view = "default";

    // Mock
    when(productUtil.buildProduct(any(), any(), any())).thenReturn(product);
    when(productRepository.save(any())).thenReturn(product);
    when(productUtil.buildProductDTO(product, productDTO, false, view)).thenReturn(productDTO);

    // Act
    ProductDTO newProductDTO = productService.create(productDTO, view);

    // Assert
    assertEquals(productDTO, newProductDTO);
    assertEquals(
        0,
        newProductDTO.getCredentials().stream()
            .filter(prop -> Objects.isNull(prop.getScope()))
            .count());
    /* All the categories while creating should be in upper case */
    assertEquals(
        newProductDTO.getTags().get(TagType.CATEGORIES).stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList()),
        newProductDTO.getTags().get(TagType.CATEGORIES));
    assertNull(newProductDTO.getFeature().getSendServiceEvents());
    assertEquals(
        newProductDTO.getFeature().getSendServiceEvents(),
        productDTO.getFeature().getSendServiceEvents());
  }
}
