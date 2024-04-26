package com.synkrato.services.partner.util;

import static com.synkrato.services.epc.common.EpcCommonConstants.DML_INSERT;
import static com.synkrato.services.partner.PartnerServiceConstants.LOAN_DELIVERY_FANNIE_DOC_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.synkrato.components.microservice.identity.IdentityContext;
import com.synkrato.components.microservice.identity.IdentityData;
import com.synkrato.services.epc.common.dto.CredentialDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.TransactionEntitlementDTO;
import com.synkrato.services.epc.common.kafka.ProductKafkaEvent.Payload;
import com.synkrato.services.partner.data.Export;
import com.synkrato.services.partner.data.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.CollectionUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProductUtilTest {

  @InjectMocks ProductUtil productUtil;

  @Spy AccessEntitlementUtil accessEntitlementUtil;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test1buildProductDTOWithWebhookView() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    String view = "WEBHOOK";

    /* Setup mock objects */

    /* Execute Test */
    ProductDTO productDTO1 = productUtil.buildProductDTO(product, new ProductDTO(), true, view);

    /* Assert test results */
    assertNotNull(productDTO1);
  }

  @Test
  public void test2buildProductDTOWithWebhookViewandNullProductDTO() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    String view = "WEBHOOK";

    /* Setup mock objects */

    /* Execute Test */
    ProductDTO productDTO1 = productUtil.buildProductDTO(product, null, true, view);

    /* Assert test results */
    assertNotNull(productDTO1);
  }

  @Test
  public void test3buildProductDTOWithoutIntegrationType() {

    /* Build Test Data */
    TestHelper.buildPartnerJWT();
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setIntegrationType(null);

    /* Setup mock objects */

    /* Execute Test */
    Product product = productUtil.buildProduct(new Product(), productDTO, DML_INSERT);

    /* Assert test results */
    assertNotNull(product.getIntegrationType());
  }

  @Test
  public void test4buildProduct() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product productEntity = TestHelper.buildProduct(productDTO);

    /* Setup mock objects */

    /* Execute Test */
    Product product = productUtil.buildProduct(productEntity, productDTO, DML_INSERT);

    /* Assert test results */
    assertNotNull(product);
  }

  @Test
  public void test5ContainDuplicateCredential() {
    /* Build Test Data */
    List<CredentialDTO> credentials =
        new ObjectMapper()
            .convertValue(
                TestHelper.buildCredential(), new TypeReference<List<CredentialDTO>>() {});

    assertFalse(productUtil.containDuplicateCredential(credentials));
  }

  @Test
  public void test6ContainDuplicateCredential() {
    /* Build Test Data */
    List<CredentialDTO> credentials =
        new ObjectMapper()
            .convertValue(
                TestHelper.buildCredential(), new TypeReference<List<CredentialDTO>>() {});

    credentials.forEach(credentialDTO -> credentialDTO.setId("id"));

    assertTrue(productUtil.containDuplicateCredential(credentials));
  }

  /** test/sandbox instance and prod product - should not throw any auth exception */
  @Test
  public void test5hasPermission() {

    /* Build Test Data */
    Product product = TestHelper.buildProduct(TestHelper.buildProductDTO());
    product.setEnvironment("prod");

    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_CLIENT_ID, "007001");
    claims.put(IdentityData.CLAIM_INSTANCE_ID, "sandbox");
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Partner");
    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);

    /* Setup mock objects */

    /* Execute Test */
    boolean hasPermission = productUtil.isAuthorized(product, false);

    /* Assert test results */
    assertTrue(hasPermission);
  }

  /** prod instance and sandbox product - should throw auth exception */
  @Test
  public void test6hasPermission() {

    /* Build Test Data */
    Product product = TestHelper.buildProduct(TestHelper.buildProductDTO());
    product.setEnvironment("sandbox");

    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_CLIENT_ID, "007001");
    claims.put(IdentityData.CLAIM_INSTANCE_ID, "prod");
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Partner");
    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);

    /* Setup mock objects */

    /* Execute Test */
    boolean hasPermission = productUtil.isAuthorized(product, false);

    /* Assert test results */
    assertFalse(hasPermission);
  }

  /** prod instance and prod product - should not throw auth exception */
  @Test
  public void test7hasPermission() {

    /* Build Test Data */
    Product product = TestHelper.buildProduct(TestHelper.buildProductDTO());
    product.setEnvironment("prod");

    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_CLIENT_ID, "007001");
    claims.put(IdentityData.CLAIM_INSTANCE_ID, "prod");
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Partner");
    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);

    /* Setup mock objects */

    /* Execute Test */
    boolean hasPermission = productUtil.isAuthorized(product, false);

    /* Assert test results */
    assertTrue(hasPermission);
  }

  /** sandbox instance and sandbox product - should not throw auth exception */
  @Test
  public void test8hasPermission() {

    /* Build Test Data */
    Product product = TestHelper.buildProduct(TestHelper.buildProductDTO());
    product.setEnvironment("sandbox");

    Map<String, Object> claims = new HashMap<>();
    claims.put(IdentityData.CLAIM_CLIENT_ID, "007001");
    claims.put(IdentityData.CLAIM_INSTANCE_ID, "sandbox");
    claims.put(IdentityData.CLAIM_IDENTITY_TYPE, "Partner");
    IdentityData identityData = new IdentityData(claims);
    IdentityContext.set(identityData);

    /* Setup mock objects */

    /* Execute Test */
    boolean hasPermission = productUtil.isAuthorized(product, false);

    /* Assert test results */
    assertTrue(hasPermission);
  }

  /** Test the building product for view OPTIONS */
  @Test
  public void testBuildProductDTOWithOptionsView() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product product = TestHelper.buildProduct(productDTO);
    product.setOptions(TestHelper.buildOptionDTO());
    String view = "OPTIONS";

    /* Execute Test */
    ProductDTO productDTO1 = productUtil.buildProductDTO(product, new ProductDTO(), true, view);

    /* Assert test results */
    assertFalse(CollectionUtils.isEmpty(productDTO1.getOptions()));
  }

  /** Test the building product for mixed case categories and duplicate categories */
  @Test
  public void testBuildProductWithoutTags() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setTags(null);
    Product productEntity = TestHelper.buildProduct(productDTO);

    /* Execute Test */
    Product product = productUtil.buildProduct(productEntity, productDTO, DML_INSERT);

    /* Assert test results */
    assertNull(product.getTags());
  }

  /** Test the building product tag query parameters for mixed case categories * */
  @Test
  public void testBuildProductTagQueryParams() {

    /* Build Test Data */
    Map<String, Object> productSearchMap = TestHelper.buildProductSearchMap();
    Map<String, List<String>> expectedResult = TestHelper.buildQueryParams();

    /* Execute Test */
    Map<String, List<String>> actualResult =
        productUtil.buildProductTagQueryParams(productSearchMap);

    /* Assert test results */
    assertEquals(expectedResult, actualResult);
  }

  /** Test the building product for skipDefaultResourceContainerCreation flag * */
  @Test
  public void testBuildProductForSkipDefaultResFlag() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product productEntity = TestHelper.buildProduct(productDTO);
    String view = "ENTITLEMENT";

    /* Execute Test */
    ProductDTO productDTO1 = productUtil.buildProductDTO(productEntity, productDTO, false, view);

    /* Assert test results */
    assertNotNull(productDTO1);
    assertNotNull(productDTO1.getEntitlements());
    assertNotNull(productDTO1.getEntitlements().getData());
    assertNotNull(productDTO1.getEntitlements().getData().getTransactions());
    assertNotNull(productDTO1.getEntitlements().getData().getTransactions().get(0));
    assertNotNull(productDTO1.getEntitlements().getData().getTransactions().get(0).getResponse());
    assertNull(
        productDTO1
            .getEntitlements()
            .getData()
            .getTransactions()
            .get(0)
            .getResponse()
            .getSkipDefaultResourceContainerCreation());
  }

  /** Test the building product for skipDefaultResourceContainerCreation flag * */
  @Test
  public void testBuildProductForSkipDefaultResFlagNotNull() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO
        .getEntitlements()
        .getData()
        .getTransactions()
        .get(0)
        .getResponse()
        .setSkipDefaultResourceContainerCreation(true);
    Product productEntity = TestHelper.buildProduct(productDTO);
    productEntity
        .getManifest()
        .getTransactions()
        .get(0)
        .getResponse()
        .setSkipDefaultResourceContainerCreation(true);

    String view = "ENTITLEMENT";

    /* Execute Test */
    ProductDTO productDTO1 = productUtil.buildProductDTO(productEntity, productDTO, false, view);

    /* Assert test results */
    assertNotNull(productDTO1);
    assertNotNull(productDTO1.getEntitlements());
    assertNotNull(productDTO1.getEntitlements().getData());
    assertNotNull(productDTO1.getEntitlements().getData().getTransactions());
    assertNotNull(productDTO1.getEntitlements().getData().getTransactions().get(0));
    assertNotNull(productDTO1.getEntitlements().getData().getTransactions().get(0).getResponse());
    assertNotNull(
        productDTO1
            .getEntitlements()
            .getData()
            .getTransactions()
            .get(0)
            .getResponse()
            .getSkipDefaultResourceContainerCreation());

    assertTrue(
        productDTO1
            .getEntitlements()
            .getData()
            .getTransactions()
            .get(0)
            .getResponse()
            .getSkipDefaultResourceContainerCreation());
  }

  /** Test building kafka payload * */
  @Test
  public void testBuildKafkaProductPayload() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product productEntity = TestHelper.buildProduct(productDTO);

    Export export = TestHelper.buildExport(LOAN_DELIVERY_FANNIE_DOC_TYPE);
    List<Export> exportList = new ArrayList<>();
    exportList.add(export);

    productEntity.getManifest().getTransactions().get(0).getRequest().setExports(exportList);

    List<String> resources = new ArrayList<>();
    resources.add("AppraisalReport");
    resources.add("Invoice");
    productEntity.getManifest().getTransactions().get(0).getResponse().setResources(resources);

    /* Execute Test */
    Payload payload = productUtil.buildKafkaProductPayload(productEntity);

    /* Assert test results */
    assertNotNull(payload);
    assertEquals(productEntity.getListingName(), payload.getListingName());
    assertEquals(productEntity.getStatus(), payload.getStatus());
    assertEquals(productEntity.getTags(), payload.getTags());
    assertEquals(productEntity.getExtensionLimit(), payload.getExtensionLimit());
    assertEquals(productEntity.getIntegrationType(), payload.getIntegrationType());
    assertEquals(productEntity.getRequestTypes(), payload.getRequestTypes());

    assertNotNull(payload.getEntitlements());
    assertNotNull(payload.getEntitlements().getAccess());
    assertNotNull(payload.getEntitlements().getData());
    assertNotNull(payload.getEntitlements().getData().getOrigin());

    List<TransactionEntitlementDTO> entitlementDTOS =
        payload.getEntitlements().getData().getTransactions();
    assertNotNull(entitlementDTOS);
    assertNotNull(entitlementDTOS.get(0));
    assertNotNull(entitlementDTOS.get(0).getRequest());
    assertNotNull(entitlementDTOS.get(0).getRequest().getExports());
    assertNull(entitlementDTOS.get(0).getRequest().getConditions());
    assertEquals(1, entitlementDTOS.get(0).getRequest().getExports().size());
    assertTrue(entitlementDTOS.get(0).getRequest().getHasFields());
    assertNotNull(entitlementDTOS.get(0).getResponse());
    assertNotNull(entitlementDTOS.get(0).getResponse().getResources());
    assertEquals(2, entitlementDTOS.get(0).getResponse().getResources().size());
    assertTrue(entitlementDTOS.get(0).getResponse().getHasFields());
    assertNull(entitlementDTOS.get(0).getResponse().getFields());
  }

  /** Test building kafka payload * */
  @Test
  public void testBuildKafkaProductPayloadWithNullValues() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product productEntity = TestHelper.buildProduct(productDTO);
    productEntity.setCredentials(null);
    productEntity.setAccessEntitlements(null);
    productEntity.getManifest().setTransactions(new ArrayList<>());

    /* Execute Test */
    Payload payload = productUtil.buildKafkaProductPayload(productEntity);

    /* Assert test results */
    assertNotNull(payload);
    assertEquals(productEntity.getListingName(), payload.getListingName());
    assertEquals(productEntity.getStatus(), payload.getStatus());
    assertEquals(productEntity.getTags(), payload.getTags());
    assertEquals(productEntity.getExtensionLimit(), payload.getExtensionLimit());
    assertEquals(productEntity.getIntegrationType(), payload.getIntegrationType());
    assertEquals(productEntity.getRequestTypes(), payload.getRequestTypes());

    assertNotNull(payload.getEntitlements());
    assertNull(payload.getEntitlements().getAccess());
    assertNotNull(payload.getEntitlements().getData());
    assertNotNull(payload.getEntitlements().getData().getOrigin());

    assertNotNull(payload.getEntitlements().getData().getTransactions());
    assertEquals(0, payload.getEntitlements().getData().getTransactions().size());
  }

  /** Test building kafka payload * */
  @Test
  public void testBuildKafkaProductPayloadWithNullFields() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    Product productEntity = TestHelper.buildProduct(productDTO);
    productEntity.setCredentials(null);
    productEntity.setAccessEntitlements(null);
    productEntity.getManifest().getTransactions().get(0).getRequest().setFields(new ArrayList<>());

    /* Execute Test */
    Payload payload = productUtil.buildKafkaProductPayload(productEntity);

    /* Assert test results */
    assertNotNull(payload);

    assertNotNull(payload.getEntitlements());
    assertNull(payload.getEntitlements().getAccess());
    assertNotNull(payload.getEntitlements().getData());
    assertNotNull(payload.getEntitlements().getData().getOrigin());

    assertNotNull(payload.getEntitlements().getData().getTransactions());
    assertFalse(
        payload.getEntitlements().getData().getTransactions().get(0).getRequest().getHasFields());
    assertNull(payload.getEntitlements().getData().getCreated());
    assertNull(payload.getEntitlements().getData().getUpdated());
    assertNull(payload.getEntitlements().getData().getCreatedBy());
    assertNull(payload.getEntitlements().getData().getUpdatedBy());
  }

  /** when feature is not passed, by default all the features should be false */
  @Test
  public void testBuildProductWithFeature1() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.setFeature(null);
    Product productEntity = new Product();

    /* Execute Test */
    Product product = productUtil.buildProduct(productEntity, productDTO, DML_INSERT);

    /* Assert test results */
    assertNotNull(product.getFeature());
    assertFalse(product.getFeature().isReceiveAutomatedTransactionUpdates());
  }

  /**
   * when feature is passed and ReceiveAutomatedTransactionUpdates is false, then product response
   * should have that value
   */
  @Test
  public void testBuildProductWithFeature2() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.getFeature().setReceiveAutomatedTransactionUpdates(false);
    Product productEntity = new Product();

    /* Execute Test */
    Product product = productUtil.buildProduct(productEntity, productDTO, DML_INSERT);

    /* Assert test results */
    assertNotNull(product.getFeature());
    assertFalse(product.getFeature().isReceiveAutomatedTransactionUpdates());
  }

  /**
   * when feature is passed and ReceiveAutomatedTransactionUpdates is true, then product response
   * should have that value
   */
  @Test
  public void testBuildProductWithFeature3() {

    /* Build Test Data */
    ProductDTO productDTO = TestHelper.buildProductDTO();
    productDTO.getFeature().setReceiveAutomatedTransactionUpdates(true);
    Product productEntity = new Product();

    /* Execute Test */
    Product product = productUtil.buildProduct(productEntity, productDTO, DML_INSERT);

    /* Assert test results */
    assertNotNull(product.getFeature());
    assertTrue(product.getFeature().isReceiveAutomatedTransactionUpdates());
  }
}
