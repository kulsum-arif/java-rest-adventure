package com.synkrato.services.partner.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.synkrato.services.partner.data.AccessEntitlement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccessEntitlementUtilTest {

  @InjectMocks AccessEntitlementUtil accessEntitlementUtil;

  @Test
  public void test1isTenantAuthorizedDeny() {
    TestHelper.buildLenderJWT();
    AccessEntitlement accessEntitlement = TestHelper.buildAccessEntitlement();

    boolean result = accessEntitlementUtil.isTenantAuthorized(accessEntitlement);

    assertFalse(result);
  }

  @Test
  public void test2isTenantAuthorizedAllow() {
    TestHelper.buildLenderJWT();
    AccessEntitlement accessEntitlement =
        AccessEntitlement.builder()
            .allow(
                "^urn:elli:encompass:instance:BE999999:.*$|^urn:elli:encompass:instance:BE1119999:.*$")
            .deny("^$")
            .build();

    boolean result = accessEntitlementUtil.isTenantAuthorized(accessEntitlement);

    assertTrue(result);
  }

  @Test
  public void test3isTenantAuthorizedConsumerAllow() {
    TestHelper.buildConsumerJWT();

    AccessEntitlement accessEntitlement =
        AccessEntitlement.builder()
            .allow(
                "^urn:elli:encompass:instance:BE999999:site:.*$|^urn:elli:encompass:instance:BE1119999:site:.*$")
            .deny("^$")
            .build();

    boolean result = accessEntitlementUtil.isTenantAuthorized(accessEntitlement);

    assertTrue(result);
  }
}
