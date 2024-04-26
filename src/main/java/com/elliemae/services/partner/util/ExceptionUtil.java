package com.synkrato.services.partner.util;

import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.MessageUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class ExceptionUtil {

  @Autowired private MessageUtil messageUtil;

  /**
   * Handle exceptions for retry
   *
   * @param e RestClientException
   * @param retryHttpStatuses retry status codes
   */
  public void handleExceptions(RestClientException e, List<HttpStatus> retryHttpStatuses) {
    log.error(e.getMessage(), e);

    handleResourceAccessException(e);

    handleHttpStatusCodeException(e, retryHttpStatuses);

    log.error("Caught HttpStatusCodeException. NonRetry-able Exception");
    throw e;
  }

  /**
   * Handle exceptions for retry
   *
   * @param e RestClientException
   * @param isSuppressNotFoundError true or false to suppress 404 http status code
   * @param retryHttpStatuses retry status codes
   */
  public void handleExceptions(
      RestClientException e, boolean isSuppressNotFoundError, List<HttpStatus> retryHttpStatuses) {
    log.error(e.getMessage(), e);

    if (e instanceof HttpStatusCodeException) {
      HttpStatus httpStatus = ((HttpStatusCodeException) e).getStatusCode();

      // ignore 404 if Suppress flag is true
      if (HttpStatus.NOT_FOUND.equals(httpStatus) && isSuppressNotFoundError) {
        log.warn("Ignoring Webhook Subscription not found");
        return;
      }
    }

    handleExceptions(e, retryHttpStatuses);

    log.error("Caught HttpStatusCodeException. NonRetry-able Exception");
    throw e;
  }

  /**
   * This method is to handle ResourceAccessException
   *
   * @param e RestClientException
   */
  private void handleResourceAccessException(RestClientException e) {
    if (e instanceof ResourceAccessException) {
      log.error("Caught ResourceAccessException. Retrying");

      throw new RetryException(e.getMessage(), e);
    }
  }

  /**
   * This method is to handle HttpStatusCodeException explicitly
   *
   * @param e RestClientException
   * @param retryHttpStatuses list of retry status codes
   */
  private void handleHttpStatusCodeException(
      RestClientException e, List<HttpStatus> retryHttpStatuses) {

    if (e instanceof HttpStatusCodeException) {
      HttpStatus httpStatus = ((HttpStatusCodeException) e).getStatusCode();

      // handle retries
      if (isRetryable(httpStatus, retryHttpStatuses)) {

        log.error("Retrying as http_status_code={}", httpStatus.toString());
        throw new RetryException(((HttpStatusCodeException) e).getResponseBodyAsString(), e);
      }

      if ((HttpStatus.FORBIDDEN.equals(httpStatus)
          || HttpStatus.UNAUTHORIZED.equals(httpStatus) && e instanceof HttpClientErrorException)) {

        throw new EpcRuntimeException(
            HttpStatus.UNAUTHORIZED,
            (ObjectUtils.isEmpty(((HttpStatusCodeException) e).getResponseBodyAsString())
                ? messageUtil.getMessage("synkrato.services.epc.unauthorized.error")
                : ((HttpStatusCodeException) e).getResponseBodyAsString()));
      }

      throw new EpcRuntimeException(httpStatus, e.getMessage());
    }
  }

  /**
   * This method is to check whether that exception is retryable or not
   *
   * @param httpStatus http status code
   * @param retryHttpStatuses retry http status codes
   * @return true/false for retry or not
   */
  private boolean isRetryable(HttpStatus httpStatus, List<HttpStatus> retryHttpStatuses) {

    return retryHttpStatuses.contains(httpStatus);
  }
}
