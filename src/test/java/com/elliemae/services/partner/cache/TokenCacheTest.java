package com.synkrato.services.partner.cache;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.synkrato.services.epc.common.business.TokenService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TokenCacheTest {

  @InjectMocks TokenCache tokenCache;

  @Mock TokenService tokenService;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test1getWebhookToken() {

    /* Build Test Data */

    /* Setup mock objects */
    when(tokenService.createWebhookToken()).thenReturn("mockToken");
    /* Execute Test */

    String token = tokenCache.getWebhookToken();

    /* Assert test results */
    assertEquals(token, "mockToken");
  }
}
