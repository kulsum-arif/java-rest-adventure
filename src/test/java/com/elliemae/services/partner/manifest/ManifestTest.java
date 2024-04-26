package com.synkrato.services.partner.manifest;

import static org.junit.Assert.assertTrue;

import com.synkrato.services.epc.common.dto.ManifestDTO;
import com.synkrato.services.epc.common.util.DataEntitlementUtil;
import com.synkrato.services.epc.common.util.JsonPathExtractorUtil;
import com.synkrato.services.epc.common.util.JsonPathUtil;
import com.synkrato.services.partner.util.TestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ManifestTest {

  @InjectMocks private DataEntitlementUtil dataEntitlementUtil;
  private String loanContent;
  private String productPricingOBManifest;
  private String appraisalManifest;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    dataEntitlementUtil.setObjectMapper(new ObjectMapper());
    dataEntitlementUtil.setJsonPathUtil(new JsonPathUtil());
    ReflectionTestUtils.setField(
        dataEntitlementUtil, "jsonPathExtractorUtil", new JsonPathExtractorUtil());
    loanContent = new String(Files.readAllBytes(Paths.get("src/test/resources/loanData.json")));
    productPricingOBManifest =
        new String(
            Files.readAllBytes(Paths.get("config/manifest/ProductandPricingOptimalBlue.json")));
    appraisalManifest =
        new String(Files.readAllBytes(Paths.get("config/manifest/AppraisalRI.json")));
  }

  @Test
  public void test1applyManifestForProductandPricingOptimalBlue_RequestManifest() throws Exception {
    /* Build Test Data */
    ManifestDTO manifestDTO = TestHelper.buildManifestDTOBasedOnManifest(productPricingOBManifest);
    manifestDTO.getTransactions().get(0).getRequestTypes().add("SEARCH");
    Map<String, Object> loan = TestHelper.getFullLoanObject(loanContent);
    Map<String, String> mapJsonPathsForValidation =
        TestHelper.buildProductPricingRequestLoanFieldsForValidation();

    /* Setup mock objects */

    /* Execute Test */
    Map<String, Object> manifestPLM =
        dataEntitlementUtil.applyManifest(manifestDTO.getTransactions().get(0).getRequest(), loan);

    /* Assert test results */
    assertTrue(TestHelper.validateManifestValues(mapJsonPathsForValidation, manifestPLM));
  }

  @Test
  public void test1applyManifestForAppraisalRI_OriginManifest() throws Exception {
    /* Build Test Data */
    ManifestDTO manifestDTO = TestHelper.buildManifestDTOBasedOnManifest(appraisalManifest);
    manifestDTO.getTransactions().get(0).getRequestTypes().add("NewOrder");
    Map<String, Object> loan = TestHelper.getFullLoanObject(loanContent);
    Map<String, String> mapJsonPathsForValidation =
        TestHelper.buildAppraisalRIOriginLoanFieldsForValidation();

    /* Setup mock objects */

    /* Execute Test */
    Map<String, Object> manifestPLM =
        dataEntitlementUtil.applyManifest(manifestDTO.getOrigin(), loan);

    /* Assert test results */
    assertTrue(TestHelper.validateManifestValues(mapJsonPathsForValidation, manifestPLM));
  }

  @Test
  public void test1applyManifestForAppraisalRI_RequestManifest() throws Exception {
    /* Build Test Data */
    ManifestDTO manifestDTO = TestHelper.buildManifestDTOBasedOnManifest(appraisalManifest);
    manifestDTO.getTransactions().get(0).getRequestTypes().add("NewOrder");
    Map<String, Object> loan = TestHelper.getFullLoanObject(loanContent);
    Map<String, String> mapJsonPathsForValidation =
        TestHelper.buildAppraisalRIRequestLoanFieldsForValidation();

    /* Setup mock objects */

    /* Execute Test */
    Map<String, Object> manifestPLM =
        dataEntitlementUtil.applyManifest(manifestDTO.getTransactions().get(0).getRequest(), loan);

    /* Assert test results */
    assertTrue(TestHelper.validateManifestValues(mapJsonPathsForValidation, manifestPLM));
  }

  @Test
  public void test1applyManifestForAppraisalRI_ResponseManifest() throws Exception {
    /* Build Test Data */
    ManifestDTO manifestDTO = TestHelper.buildManifestDTOBasedOnManifest(appraisalManifest);
    manifestDTO.getTransactions().get(0).getRequestTypes().add("NewOrder");
    Map<String, Object> loan = TestHelper.getFullLoanObject(loanContent);
    Map<String, String> mapJsonPathsForValidation =
        TestHelper.buildAppraisalRIResponseLoanFieldsForValidation();

    /* Setup mock objects */

    /* Execute Test */
    Map<String, Object> manifestPLM =
        dataEntitlementUtil.applyManifest(manifestDTO.getTransactions().get(0).getResponse(), loan);

    /* Assert test results */
    assertTrue(TestHelper.validateManifestValues(mapJsonPathsForValidation, manifestPLM));
  }
}
