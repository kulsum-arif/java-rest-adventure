package com.synkrato.services.partner.task;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class CreateOapiWebhookSubscriptionTaskTest {

  @InjectMocks CreateOapiWebhookSubscriptionTask createOapiWebhookSubscriptionTask;

  @Mock RestTemplate restTemplate;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGet() {

    /* Build Test Data */
    boolean isException = false;
    /* Execute Test */

    try {
      Map<String, Object> payload = createOapiWebhookSubscriptionTask.get();
    } catch (Exception ex) {
      isException = true;
    }
    /* Assert test results */
    assertTrue(isException);
  }
}
