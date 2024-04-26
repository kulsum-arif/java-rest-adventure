package com.synkrato.services.partner.task;

import static com.synkrato.services.epc.common.EpcCommonConstants.EMPTY_STRING;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_ERROR;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_PROCESSING_TIME;
import static com.synkrato.services.epc.common.EpcCommonConstants.SLASH_CHAR;
import static com.synkrato.services.partner.PartnerServiceConstants.SUBSCRIPTION_ID;
import static com.synkrato.services.partner.PartnerServiceConstants.UNAUTHORIZED_MESSAGE;

import com.synkrato.services.epc.common.error.EpcRuntimeException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@Slf4j
public class CreateOapiWebhookSubscriptionTask implements Supplier<Map<String, Object>> {

  private RestTemplate restTemplate;
  private Map<String, Object> createSubscription;
  HttpEntity<String> httpRequest;
  private String createSubscriptionUrl;

  @Override
  public Map get() {

    try {
      StopWatch processingTimeStopWatch = new StopWatch();
      processingTimeStopWatch.start();
      ResponseEntity<Map<String, String>> response =
          restTemplate.exchange(
              createSubscriptionUrl,
              HttpMethod.POST,
              httpRequest,
              new ParameterizedTypeReference<Map<String, String>>() {});
      processingTimeStopWatch.stop();
      List<String> locationHeaderList = response.getHeaders().get("Location");
      String subscriptionLocationUrl =
          !CollectionUtils.isEmpty(locationHeaderList) ? locationHeaderList.get(0) : EMPTY_STRING;
      String subscriptionId =
          StringUtils.isEmpty(subscriptionLocationUrl)
              ? null
              : subscriptionLocationUrl.substring(
                  subscriptionLocationUrl.lastIndexOf(SLASH_CHAR) + 1);
      createSubscription.put(SUBSCRIPTION_ID, subscriptionId);
      if (response.getStatusCode() == HttpStatus.OK
          || response.getStatusCode() == HttpStatus.CREATED) {
        log.info(
            "Create Subscription Successful for {}{} ",
            LOGGER_PROCESSING_TIME,
            processingTimeStopWatch.getTotalTimeMillis());
      } else {
        log.error(
            "Create Subscription status_code={}",
            response.getStatusCode());
      }
    } catch (RestClientException rce) {
      log.error("Create Subscription failed with {}", rce.getMessage(), rce);
      handleExceptions(rce);
    }

    return createSubscription;
  }

  /**
   * Handle Exceptions
   *
   * @param rce RestclientException
   */
  private void handleExceptions(RestClientException rce) {
    log.error("Create Subscription Handle Exception={}", rce.getMessage(), rce);

    if (rce instanceof HttpClientErrorException) {
      HttpClientErrorException httpClientErrorException = (HttpClientErrorException) rce;
      if (httpClientErrorException.getStatusCode().is5xxServerError()) {
        throw rce;
      } else if (HttpStatus.FORBIDDEN.equals(httpClientErrorException.getStatusCode())
          || HttpStatus.UNAUTHORIZED.equals(httpClientErrorException.getStatusCode())) {
        throw new EpcRuntimeException(
            HttpStatus.UNAUTHORIZED,
            (StringUtils.isEmpty(httpClientErrorException.getResponseBodyAsString())
                ? UNAUTHORIZED_MESSAGE
                : httpClientErrorException.getResponseBodyAsString()));
      } else {
        throw new EpcRuntimeException(
            HttpStatus.BAD_REQUEST, httpClientErrorException.getResponseBodyAsString());
      }
    } else {
      throw rce;
    }
  }
}
