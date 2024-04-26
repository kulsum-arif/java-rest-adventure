package com.synkrato.services.partner.util;

import static org.junit.Assert.assertEquals;

import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.partner.data.BillingRule;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BillingRuleUtilTest {

  @InjectMocks BillingRuleUtil billingRuleUtil;

  @Test
  public void buildBillingRuleTest() {

    // Arrange

    UUID productId = UUID.randomUUID();
    UUID billingRuleId = UUID.randomUUID();
    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setId(billingRuleId);
    billingRule.setProductId(productId);
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.setProductId(productId.toString());
    billingRuleDTO.setId(billingRuleId.toString());

    // Act
    BillingRule result = billingRuleUtil.buildBillingRule(billingRuleDTO);

    // Assert
    assertEquals(billingRule.getProductId(), result.getProductId());
    assertEquals(billingRule.getStatus(), result.getStatus().toLowerCase());
  }

  @Test
  public void buildBillingRuleWithEntityTest() {

    // Arrange

    UUID productId = UUID.randomUUID();
    UUID billingRuleId = UUID.randomUUID();
    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setId(billingRuleId);
    billingRule.setProductId(productId);
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.setProductId(productId.toString());
    billingRuleDTO.setId(billingRuleId.toString());

    // Act
    BillingRule result = billingRuleUtil.buildBillingRule(billingRule, billingRuleDTO);

    // Assert
    assertEquals(billingRule.getProductId(), result.getProductId());
    assertEquals(billingRule.getStatus(), result.getStatus());
  }

  @Test
  public void buildBillingRuleDTOTest() {

    // Arrange
    UUID productId = UUID.randomUUID();
    UUID billingRuleId = UUID.randomUUID();
    BillingRule billingRule = TestHelper.buildBillingRule();
    billingRule.setId(billingRuleId);
    billingRule.setProductId(productId);
    billingRule.setStatus("DRAFT");
    BillingRuleDTO billingRuleDTO = TestHelper.buildBillingRuleDTO();
    billingRuleDTO.setProductId(productId.toString());
    billingRuleDTO.setId(billingRuleId.toString());

    // Act
    BillingRuleDTO result = billingRuleUtil.buildBillingRuleDTO(billingRule);

    // Assert
    assertEquals(billingRule.getStatus(), result.getStatus().toString());
  }

  @Test
  public void buildBillingRulesDTOListTest() {

    // Arrange
    List<BillingRule> billingRules = TestHelper.buildBillingRulesList();

    // Act
    List<BillingRuleDTO> result = billingRuleUtil.buildBillingRulesDTO(billingRules);

    // Assert
    assertEquals(billingRules.size(), result.size());
  }
}
