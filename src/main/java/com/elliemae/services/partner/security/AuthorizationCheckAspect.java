package com.synkrato.services.partner.security;

import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * This aspect will check the authorization for the operations to be performed on service
 *
 * @author bappala
 */
@Slf4j
@Component
@Aspect
public class AuthorizationCheckAspect {

  @Autowired private MessageUtil messageUtil;
  private static final String UNAUTHORIZED_ERROR = "synkrato.services.epc.unauthorized.error";

  @Before(
      "within(com.synkrato.services.partner.controller.*) && execution(public * *(..)) && @annotation(authCheck)")
  public void performAuthCheck(Authorization authCheck) {

    switch (authCheck.permission()) {
      case GET_PRODUCT:
        if (!CommonUtil.hasValidToken() && !CommonUtil.hasConsumerToken()) {
          throwUnauthorizedException();
        }
        break;
      case UPDATE_PRODUCT:
        if (!CommonUtil.hasValidProductUpdateToken()) {
          throwUnauthorizedException();
        }
        break;
      case CREATE_PRODUCT:
        if (!CommonUtil.hasPartnerToken() && !CommonUtil.hasInternalAdminToken()) {
          throwUnauthorizedException();
        }
        break;
      default:
        break;
    }
    log.debug(
        "Current user has the permission : {}. So, proceeding to perform the operation.",
        authCheck.permission());
  }

  private void throwUnauthorizedException() {
    throw new EpcRuntimeException(
        HttpStatus.UNAUTHORIZED, messageUtil.getMessage(UNAUTHORIZED_ERROR));
  }

  public void setMessageUtil(MessageUtil messageUtil) {
    this.messageUtil = messageUtil;
  }
}
