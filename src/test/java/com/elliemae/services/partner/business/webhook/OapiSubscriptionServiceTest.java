package com.synkrato.services.partner.business.webhook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.synkrato.services.epc.common.dto.WebhookDTO;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.impl.webhook.OapiSubscriptionServiceImpl;
import com.synkrato.services.partner.cache.TokenCache;
import com.synkrato.services.partner.task.CreateOapiWebhookSubscriptionTask;
import com.synkrato.services.partner.task.UpdateOapiWebhookSubscriptionTask;
import com.synkrato.services.partner.util.ExceptionUtil;
import com.synkrato.services.partner.util.TestHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class OapiSubscriptionServiceTest {

  @InjectMocks OapiSubscriptionServiceImpl oapiSubscriptionService;
  @Mock TokenCache tokenCache;
  @Mock RestTemplate restTemplate;
  @Mock MessageUtil messageUtil;
  @Spy ExceptionUtil exceptionUtil;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(
        oapiSubscriptionService,
        "getSubscriptionUrl",
        "http://localhost/v1/subscriptions/{subscriptionId}");

    ReflectionTestUtils.setField(exceptionUtil, "messageUtil", messageUtil);
    ReflectionTestUtils.setField(oapiSubscriptionService, "exceptionUtil", exceptionUtil);
  }

  @Test
  public void test1CreateSubscriptionException() {

    /* Build Test Data */
    Map<String, Object> payload = TestHelper.buildSubscriptionPayload();
    List<Map<String, Object>> completePayload = TestHelper.completePayload();
    CreateOapiWebhookSubscriptionTask createOapiWebhookSubscriptionTask =
        mock(CreateOapiWebhookSubscriptionTask.class);

    /* Setup mock objects */
    when(tokenCache.getWebhookToken()).thenReturn("w4PNXH33Cz9qYLVfPS9e48zuoKAn");

    boolean isException = false;

    /* Execute Test */
    try {
      List<WebhookDTO> subscriptionList =
          oapiSubscriptionService.createSubscription(completePayload);
    } catch (Exception ex) {
      isException = true;
    }
    /* Assert test results */
    assertTrue(isException);
  }

  @Test
  public void test2GetSubscriptionException() {

    /* Build Test Data */
    Map<String, Object> payload = TestHelper.buildSubscriptionPayload();
    CreateOapiWebhookSubscriptionTask createOapiWebhookSubscriptionTask =
        mock(CreateOapiWebhookSubscriptionTask.class);
    String subscriptionId = "mockId";

    /* Setup mock objects */
    when(tokenCache.getWebhookToken()).thenReturn("w4PNXH33Cz9qYLVfPS9e48zuoKAn");

    boolean isException = false;

    /* Execute Test */
    try {
      WebhookDTO webhookDTO = oapiSubscriptionService.getSubscription(subscriptionId);
    } catch (Exception ex) {
      isException = true;
    }
    /* Assert test results */
    assertTrue(isException);
  }

  @Test
  public void test3GeleteSubscriptionException() {

    /* Build Test Data */
    Map<String, Object> payload = TestHelper.buildSubscriptionPayload();
    CreateOapiWebhookSubscriptionTask createOapiWebhookSubscriptionTask =
        mock(CreateOapiWebhookSubscriptionTask.class);
    List<String> stringList = new ArrayList<>();
    stringList.add("subscriptionId1");
    stringList.add("subscriptionId2");

    /* Setup mock objects */
    when(tokenCache.getWebhookToken()).thenReturn("w4PNXH33Cz9qYLVfPS9e48zuoKAn");

    boolean isException = false;

    /* Execute Test */
    try {
      oapiSubscriptionService.deleteSubscriptions(stringList);
    } catch (Exception ex) {
      isException = true;
    }
    /* Assert test results */
    assertFalse(isException);
  }

  @Test(expected = Exception.class)
  public void test4UpdateSubscriptionException() {

    /* Build Test Data */
    Map<String, Object> payload = TestHelper.buildSubscriptionPayload();
    List<Map<String, Object>> completePayload = TestHelper.completePayload();

    UpdateOapiWebhookSubscriptionTask updateOapiWebhookSubscriptionTask =
        mock(UpdateOapiWebhookSubscriptionTask.class);

    /* Execute Test */
    oapiSubscriptionService.updateSubscription(completePayload);
  }

  /** Get Webhook Subscription throws 404 - Get Webhook returns Webhook DTO with error message */
  @Test
  public void test5GetSubscriptionNotFound() {

    /* Build Test Data */

    /* Setup mock objects */
    when(tokenCache.getWebhookToken()).thenReturn("w4PNXH33Cz9qYLVfPS9e48zuoKAn");
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            Matchers.eq(new ParameterizedTypeReference<Map<String, Object>>() {})))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    /* Execute Test */
    WebhookDTO webhookDTO = oapiSubscriptionService.getSubscription("-1");

    /* Assert Test Results */
    assertEquals("-1", webhookDTO.getSubscriptionId());
    assertEquals("Webhook Subscription not found", webhookDTO.getUrl());
  }

  /** Delete Webhook Subscription throws 404 but exception is suppressed */
  @Test
  public void test6DeleteSubscriptionNotFound() {

    /* Build Test Data */
    List<String> deleteSubscriptions = new ArrayList<>();
    deleteSubscriptions.add("-1");

    /* Setup mock objects */
    when(tokenCache.getWebhookToken()).thenReturn("w4PNXH33Cz9qYLVfPS9e48zuoKAn");
    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    /* Execute Test */
    boolean isException = false;
    try {
      oapiSubscriptionService.deleteSubscriptions(deleteSubscriptions);
    } catch (Exception e) {
      isException = true;
    }

    /* Assert Test Results */
    assertFalse(isException);
  }

  /** Delete Webhook Subscription throws Unauthorized error */
  @Test(expected = EpcRuntimeException.class)
  public void test7DeleteSubscriptionUnAuthorizedError() {

    /* Build Test Data */
    List<String> deleteSubscriptions = new ArrayList<>();
    deleteSubscriptions.add("-1");

    /* Setup mock objects */
    when(tokenCache.getWebhookToken()).thenReturn("w4PNXH33Cz9qYLVfPS9e48zuoKAn");
    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
    when(messageUtil.getMessage("synkrato.services.epc.unauthorized.error"))
        .thenReturn("Unauthorized");

    /* Execute Test */
    oapiSubscriptionService.deleteSubscriptions(deleteSubscriptions);
  }

  /** Get Webhook Subscription throws 503 - Get Webhook returns Webhook DTO with error message */
  @Test(expected = RetryException.class)
  public void test8GetSubscriptionServiceUnavailable() {

    /* Build Test Data */
    /* Setup mock objects */
    when(tokenCache.getWebhookToken()).thenReturn("w4PNXH33Cz9qYLVfPS9e48zuoKAn");
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            Matchers.eq(new ParameterizedTypeReference<Map<String, Object>>() {})))
        .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

    /* Execute Test */
    oapiSubscriptionService.getSubscription("-1");
  }

  /** Get Webhook Subscription throws 504 - Get Webhook returns Webhook DTO with error message */
  @Test(expected = RetryException.class)
  public void test9GetSubscriptionGatewayTimeOut() {

    /* Build Test Data */
    /* Setup mock objects */
    when(tokenCache.getWebhookToken()).thenReturn("w4PNXH33Cz9qYLVfPS9e48zuoKAn");
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            Matchers.eq(new ParameterizedTypeReference<Map<String, Object>>() {})))
        .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

    /* Execute Test */
    oapiSubscriptionService.getSubscription("-1");
  }

  /** This is a test for retries in case of 5xx failures */
  @Test(expected = EpcRuntimeException.class)
  public void testRetryForDeleteSubscriptionWith4xx() {

    // Arrange
    List<String> subscriptions = new ArrayList<>();
    subscriptions.add(UUID.randomUUID().toString());

    HttpEntity<String> httpRequest = new HttpEntity(null);

    // Mock
    when(tokenCache.getWebhookToken()).thenReturn("w4PNXH33Cz9qYLVfPS9e48zuoKAn");
    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
        .thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST));

    // Act
    oapiSubscriptionService.deleteSubscriptions(subscriptions);
  }

  /** This is a test for retries in case of 5xx failures */
  @Test(expected = RetryException.class)
  public void testRetryForDeleteSubscriptionWith5xx() {

    // Arrange
    List<String> subscriptions = new ArrayList<>();
    subscriptions.add(UUID.randomUUID().toString());

    HttpEntity<String> httpRequest = new HttpEntity(null);

    // Mock
    when(tokenCache.getWebhookToken()).thenReturn("w4PNXH33Cz9qYLVfPS9e48zuoKAn");
    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
        .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

    // Act
    oapiSubscriptionService.deleteSubscriptions(subscriptions);
  }
}
