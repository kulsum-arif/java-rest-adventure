package com.synkrato.services.partner.business.impl.webhook;

import static com.synkrato.services.epc.common.EpcCommonConstants.AUTH_BEARER_PREFIX;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_PROCESSING_TIME;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;
import static com.synkrato.services.partner.PartnerServiceConstants.ENDPOINT;
import static com.synkrato.services.partner.PartnerServiceConstants.EVENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.RESOURCE;
import static com.synkrato.services.partner.PartnerServiceConstants.SIGNING_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.SUBSCRIPTION_ID;

import com.synkrato.components.microservice.web.filter.CorrelationIdFilter;
import com.synkrato.services.epc.common.dto.WebhookDTO;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.webhook.SubscriptionService;
import com.synkrato.services.partner.cache.TokenCache;
import com.synkrato.services.partner.task.CreateOapiWebhookSubscriptionTask;
import com.synkrato.services.partner.task.UpdateOapiWebhookSubscriptionTask;
import com.synkrato.services.partner.util.ExceptionUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OapiSubscriptionServiceImpl implements SubscriptionService {

  private static final String LOGGER_SUBSCRIPTION_ID = "SUBSCRIPTION_ID=";
  private static final String SUBSCRIPTION_ERROR = "Webhook Subscription not found";
  private static final String SUBSCRIPTION_ID_PLACEHOLDER = "{subscriptionId}";
  private static final String MAX_RETRY = "#{${synkrato.em-services.webhook.max-attempt:3}}";
  private static final String RETRY_DELAY = "#{${synkrato.emservices.webhook.retry-delay:1000}}";

  private static final List<HttpStatus> RETRY_HTTP_STATUSES =
      Collections.unmodifiableList(
          Arrays.asList(
              HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT));

  @Autowired private MessageUtil messageUtil;
  @Autowired private TokenCache tokenCache;
  @Autowired private RestTemplate restTemplate;
  @Autowired private ExceptionUtil exceptionUtil;

  @Value("${synkrato.em-services.endpoint-url.oapi-webhook.create-subscription}")
  private String createSubscriptionUrl;

  @Value("${synkrato.em-services.endpoint-url.oapi-webhook.get-subscription}")
  private String getSubscriptionUrl;

  @Override
  @Retryable(
      maxAttemptsExpression = MAX_RETRY,
      value = {RetryException.class},
      backoff = @Backoff(maxDelayExpression = RETRY_DELAY, multiplier = 2))
  public List<WebhookDTO> createSubscription(List<Map<String, Object>> createSubscriptionsList) {

    List<WebhookDTO> webhookDtoList = null;
    log.debug("create_subscription {}", LOGGER_START);

    if (!CollectionUtils.isEmpty(createSubscriptionsList)) {
      webhookDtoList = createOapiWebhookSubscription(createSubscriptionsList);
    }
    log.debug("create_subscription {}", LOGGER_END);
    return webhookDtoList;
  }

  /**
   * Get Webhook Subscription Details
   *
   * @param subscriptionId Webhook Subscription Id
   * @return WebhookDTO
   */
  @Override
  @Retryable(
      maxAttemptsExpression = MAX_RETRY,
      value = {RetryException.class},
      backoff = @Backoff(maxDelayExpression = RETRY_DELAY, multiplier = 2))
  public WebhookDTO getSubscription(String subscriptionId) {
    log.debug("get_subscription {}", LOGGER_START);
    log.info("get subscription by subscription_id={}", subscriptionId);
    WebhookDTO webhookDTO = null;

    try {
      MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
      multiValueMap.add(
          HttpHeaders.AUTHORIZATION, AUTH_BEARER_PREFIX + tokenCache.getWebhookToken());
      multiValueMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      multiValueMap.add(
          CorrelationIdFilter.KEY_CORRELATION_ID, MDC.get(CorrelationIdFilter.KEY_CORRELATION_ID));
      HttpEntity<String> httpRequest = new HttpEntity(multiValueMap);
      StopWatch processingTimeStopWatch = new StopWatch();
      processingTimeStopWatch.start();
      String subscriptionUrl =
          CommonUtil.getServiceUrl(getSubscriptionUrl, SUBSCRIPTION_ID_PLACEHOLDER, subscriptionId);
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              subscriptionUrl,
              HttpMethod.GET,
              httpRequest,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      processingTimeStopWatch.stop();

      if (!CollectionUtils.isEmpty(response.getBody())) {
        webhookDTO = buildWebhookDTO(response.getBody());
      }

      log.info(
          "Get Webhook Subscription status_code={} {}{}",
          response.getStatusCode(),
          LOGGER_PROCESSING_TIME,
          processingTimeStopWatch.getTotalTimeMillis());
    } catch (RestClientException rce) {
      exceptionUtil.handleExceptions(rce, true, RETRY_HTTP_STATUSES);
      webhookDTO = buildErrorWebhookDTO(subscriptionId);
    }
    log.debug("get_subscription {}", LOGGER_END);
    return webhookDTO;
  }

  @Override
  @Retryable(
      maxAttemptsExpression = MAX_RETRY,
      value = {RetryException.class},
      backoff = @Backoff(maxDelayExpression = RETRY_DELAY, multiplier = 2))
  public void updateSubscription(List<Map<String, Object>> updateSubscriptionsList) {
    log.debug("update_subscription {}", LOGGER_START);
    if (!CollectionUtils.isEmpty(updateSubscriptionsList)) {
      updateOapiWebhookSubscription(updateSubscriptionsList);
    }
    log.debug("update_subscription {}", LOGGER_END);
  }

  /**
   * Delete subscriptions
   *
   * @param subscriptionIds List of subscriptions ids to be deleted
   */
  @Override
  @Retryable(
      maxAttemptsExpression = MAX_RETRY,
      value = {RetryException.class},
      backoff = @Backoff(maxDelayExpression = RETRY_DELAY, multiplier = 2))
  public void deleteSubscriptions(List<String> subscriptionIds) {
    if (!CollectionUtils.isEmpty(subscriptionIds)) {
      String webhookToken = tokenCache.getWebhookToken();
      for (String subscriptionId : subscriptionIds) {
        deleteOapiWebhookSubscription(subscriptionId, webhookToken);
      }
    }
  }

  /**
   * Create a webhook subscription with OAAP/Platform Shared Services
   *
   * @param createSubscriptionsList Product details
   * @return List of subscription ids
   */
  private List<WebhookDTO> createOapiWebhookSubscription(
      List<Map<String, Object>> createSubscriptionsList) {

    List<WebhookDTO> webhookDtoList = new ArrayList<>();
    List<CompletableFuture<Map<String, Object>>> completableFutureList = new ArrayList<>();
    for (Map<String, Object> createSubscription : createSubscriptionsList) {
      MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
      multiValueMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      multiValueMap.add(
          HttpHeaders.AUTHORIZATION, AUTH_BEARER_PREFIX + tokenCache.getWebhookToken());
      multiValueMap.add(
          CorrelationIdFilter.KEY_CORRELATION_ID, MDC.get(CorrelationIdFilter.KEY_CORRELATION_ID));
      HttpEntity<String> httpRequest = new HttpEntity(createSubscription, multiValueMap);
      CompletableFuture<Map<String, Object>> completableFuture =
          CompletableFuture.supplyAsync(
              new CreateOapiWebhookSubscriptionTask(
                  restTemplate, createSubscription, httpRequest, createSubscriptionUrl));
      completableFutureList.add(completableFuture);
    }

    try {
      CompletableFuture.allOf(
              completableFutureList.toArray(new CompletableFuture[completableFutureList.size()]))
          .join();
      for (CompletableFuture<Map<String, Object>> completableFuture : completableFutureList) {
        Map<String, Object> createSubscription = completableFuture.get();
        webhookDtoList.add(buildWebhookDTO(createSubscription));
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      log.error("Interrupted Exception occurred {}", ie.getCause().getMessage(), ie);
      throw new EpcRuntimeException(ie.getCause().getMessage(), ie);
    } catch (ExecutionException ee) {
      log.error("Execution Exception occurred {}", ee.getCause().getMessage(), ee);
      throw new EpcRuntimeException(ee.getCause().getMessage(), ee);
    } catch (CompletionException ce) {
      log.error("Completion Exception occurred {}", ce.getCause().getMessage(), ce);
      if (ce.getCause() instanceof EpcRuntimeException) {
        throw new EpcRuntimeException(
            ((EpcRuntimeException) ce.getCause()).getHttpStatus(), ce.getCause().getMessage());
      } else if (ce.getCause() instanceof HttpServerErrorException) {
        throw new EpcRuntimeException(
            ((HttpServerErrorException) ce.getCause()).getStatusCode(), ce.getCause().getMessage());
      } else {
        throw new EpcRuntimeException(ce.getCause().getMessage(), ce);
      }
    }

    return webhookDtoList;
  }

  /**
   * Delete OAPI Webhook Subscription
   *
   * @param subscriptionId Subscription id to be deleted
   */
  private void deleteOapiWebhookSubscription(String subscriptionId, String webhookToken) {
    log.debug("delete_oapi_webhook_subscription {}", LOGGER_START);
    try {
      MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
      multiValueMap.add(HttpHeaders.AUTHORIZATION, AUTH_BEARER_PREFIX + webhookToken);
      multiValueMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      multiValueMap.add(
          CorrelationIdFilter.KEY_CORRELATION_ID, MDC.get(CorrelationIdFilter.KEY_CORRELATION_ID));
      HttpEntity<String> httpRequest = new HttpEntity(multiValueMap);
      String deleteSubscriptionUrl =
          CommonUtil.getServiceUrl(getSubscriptionUrl, SUBSCRIPTION_ID_PLACEHOLDER, subscriptionId);
      restTemplate.exchange(deleteSubscriptionUrl, HttpMethod.DELETE, httpRequest, Void.class);

      log.warn("Deleted Webhook Subscription for subscription_id={}", subscriptionId);

    } catch (RestClientException rce) {
      exceptionUtil.handleExceptions(rce, true, RETRY_HTTP_STATUSES);
    }

    log.debug("delete_oapi_webhook_subscription {}", LOGGER_END);
  }

  /**
   * Update a webhook subscription with OAPI/Platform Shared Services
   *
   * @param updateSubscriptionsList Product details
   */
  private void updateOapiWebhookSubscription(List<Map<String, Object>> updateSubscriptionsList) {
    List<CompletableFuture<ResponseEntity>> completableFutureList = new ArrayList<>();
    for (Map<String, Object> updateSubscription : updateSubscriptionsList) {
      MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
      multiValueMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      multiValueMap.add(
          HttpHeaders.AUTHORIZATION, AUTH_BEARER_PREFIX + tokenCache.getWebhookToken());
      multiValueMap.add(
          CorrelationIdFilter.KEY_CORRELATION_ID, MDC.get(CorrelationIdFilter.KEY_CORRELATION_ID));
      String subscriptionUrl =
          CommonUtil.getServiceUrl(
              getSubscriptionUrl,
              SUBSCRIPTION_ID_PLACEHOLDER,
              updateSubscription.get(SUBSCRIPTION_ID).toString());
      HttpEntity<String> httpRequest = new HttpEntity(updateSubscription, multiValueMap);
      CompletableFuture<ResponseEntity> completableFuture =
          CompletableFuture.supplyAsync(
              new UpdateOapiWebhookSubscriptionTask(restTemplate, httpRequest, subscriptionUrl));
      completableFutureList.add(completableFuture);
    }
    try {
      CompletableFuture.allOf(
              completableFutureList.toArray(new CompletableFuture[completableFutureList.size()]))
          .join();
    } catch (CompletionException ce) {
      log.error("Completion Exception occurred {}", ce.getCause().getMessage(), ce);
      if (ce.getCause() instanceof EpcRuntimeException) {
        throw new EpcRuntimeException(
            ((EpcRuntimeException) ce.getCause()).getHttpStatus(), ce.getCause().getMessage());
      } else if (ce.getCause() instanceof HttpServerErrorException) {
        throw new EpcRuntimeException(
            ((HttpServerErrorException) ce.getCause()).getStatusCode(), ce.getCause().getMessage());
      } else {
        throw new EpcRuntimeException(ce.getCause().getMessage(), ce);
      }
    } catch (RestClientException rce) {
      exceptionUtil.handleExceptions(rce, RETRY_HTTP_STATUSES);
    }
  }

  /**
   * Build Webhook DTO from OAPI get webhook response
   *
   * @param webhookResponse Get Webhook response from OAPI
   * @return WebhookDTO
   */
  private WebhookDTO buildWebhookDTO(Map<String, Object> webhookResponse) {

    return WebhookDTO.builder()
        .subscriptionId((String) webhookResponse.get(SUBSCRIPTION_ID))
        .url((String) webhookResponse.get(ENDPOINT))
        .signingkey((String) webhookResponse.get(SIGNING_KEY))
        .events((List<String>) webhookResponse.get(EVENTS))
        .resource((String) webhookResponse.get(RESOURCE))
        .build();
  }

  /**
   * Build WebhookDTO with missing subscription error message in the url attribute
   *
   * @param subscriptionId Missing subscription id
   * @return WebhookDTO with missing error in the url attribute
   */
  private WebhookDTO buildErrorWebhookDTO(String subscriptionId) {
    log.warn("Webhook Subscription Not Found for {}{}", LOGGER_SUBSCRIPTION_ID, subscriptionId);

    return WebhookDTO.builder().subscriptionId(subscriptionId).url(SUBSCRIPTION_ERROR).build();
  }
}
