package com.synkrato.services.partner.task;

import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_ERROR;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_PROCESSING_TIME;
import static com.synkrato.services.partner.PartnerServiceConstants.UNAUTHORIZED_MESSAGE;

import com.synkrato.services.epc.common.error.EpcRuntimeException;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@Slf4j
public class UpdateOapiWebhookSubscriptionTask implements Supplier<ResponseEntity> {

  private RestTemplate restTemplate;
  HttpEntity<String> httpRequest;
  private String getSubscriptionUrl;

  @Override
  public ResponseEntity get() {
    ResponseEntity<Void> response = null;
    try {
      StopWatch processingTimeStopWatch = new StopWatch();
      processingTimeStopWatch.start();
      response = restTemplate.exchange(getSubscriptionUrl, HttpMethod.PUT, httpRequest, Void.class);
      processingTimeStopWatch.stop();
      if (response.getStatusCode() == HttpStatus.OK) {
        log.info(
            "Update Subscription Successful for {}{}",
            LOGGER_PROCESSING_TIME,
            processingTimeStopWatch.getTotalTimeMillis());
      }
    } catch (RestClientException rce) {
      log.error("Update Subscription {}", rce.getMessage(), rce);
      handleExceptions(rce);
    }
    return response;
  }

  /**
   * Handle Exceptions
   *
   * @param rce RestclientException
   */
  private void handleExceptions(RestClientException rce) {
    log.error("Update Subscription Handle Exception={}", rce.getMessage(), rce);

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
