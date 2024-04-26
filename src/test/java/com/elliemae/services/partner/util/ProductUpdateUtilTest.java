package com.synkrato.services.partner.util;

import static com.synkrato.services.partner.PartnerServiceConstants.ENTITLEMENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.EXTENSION_LIMIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.partner.validators.ProductUpdateValidator;
import com.synkrato.services.partner.validators.ProductValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class ProductUpdateUtilTest {

  @InjectMocks ProductUpdateUtil productUpdateUtil;
  @Spy ProductUpdateValidator productUpdateValidator;
  @Spy ProductValidator productValidator;
  @Spy ObjectMapper objectMapper;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    productUpdateValidator.setObjectMapper(new ObjectMapper());
    productUpdateValidator.setProductUtil(new ProductUtil());
  }

  /** test premerge validation success */
  @Test
  public void test1PremergeValidationSuccess() {
    boolean success = true;

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Map<String, Object> updateProductMap = TestHelper.buildAsyncProductMap();
    updateProductMap.remove(ENTITLEMENTS);
    updateProductMap.remove(EXTENSION_LIMIT);
    productUpdateValidator = new ProductUpdateValidator();
    /* Setup mock objects */

    /* Execute Test */
    try {
      productUpdateUtil.premergeValidate(updateProductMap, productDTO);
    } catch (Exception ex) {
      success = false;
    }

    /* Assert test results */
    assertTrue(success);
  }

  /** test premerge validation fail on passing Data entitlments by partner */
  @Test(expected = Exception.class)
  public void test2PremergeValidationFailure() throws Exception {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Map<String, Object> updateProductMap = TestHelper.buildAsyncProductMap();
    productUpdateValidator = new ProductUpdateValidator();

    /* Execute Test */
    productUpdateUtil.premergeValidate(updateProductMap, productDTO);
  }

  /** test premerge validation failure */
  @Test(expected = Exception.class)
  public void test3PostmergeValidationFailure() throws Exception {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productValidator = new ProductValidator();
    /* Setup mock objects */

    /* Execute Test */
    productUpdateUtil.postmergeValidate(productDTO);
  }

  /** test merge method */
  @Test
  public void test4MergeSuccess() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Map<String, Object> updateProductMap = TestHelper.buildAsyncProductMap();

    /* Setup mock objects */
    objectMapper = new ObjectMapper();

    /* Execute Test */
    ProductDTO newProductDto = productUpdateUtil.merge(updateProductMap, productDTO);

    /* Assert test results */
    assertNotNull(newProductDto);
    assertEquals(newProductDto.getId(), productDTO.getId());
    assertEquals(newProductDto.getListingName(), productDTO.getListingName());
  }

  /** test merge failure */
  @Test(expected = Exception.class)
  public void test5MergeFailure() throws Exception {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Map<String, Object> updateProductMap = TestHelper.buildAsyncProductMap();
    updateProductMap.put("integrationType", "abc");

    /* Setup mock objects */
    objectMapper = new ObjectMapper();

    /* Execute Test */
    productUpdateUtil.premergeValidate(updateProductMap, productDTO);
  }

  /** merge failure - update tags with invalid content */
  @Test(expected = Exception.class)
  public void test6MergeFailure() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Map<String, Object> updateProductMap = TestHelper.buildAsyncProductMap();
    updateProductMap.put("tags", "");

    /* Setup mock objects */
    objectMapper = new ObjectMapper();

    /* Execute Test */
    productUpdateUtil.merge(updateProductMap, productDTO);
  }

  /** merge failure - update requestTypes with invalid content */
  @Test(expected = Exception.class)
  public void test7MergeFailure() {
    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Map<String, Object> updateProductMap = TestHelper.buildAsyncProductMap();
    updateProductMap.put("requestTypes", "");

    /* Setup mock objects */
    objectMapper = new ObjectMapper();

    /* Execute Test */
    productUpdateUtil.merge(updateProductMap, productDTO);
  }
}
