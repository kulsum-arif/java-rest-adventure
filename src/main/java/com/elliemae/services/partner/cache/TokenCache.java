package com.synkrato.services.partner.cache;

import static com.synkrato.services.partner.PartnerServiceConstants.WEBHOOK_TOKEN_CACHE;

import com.synkrato.services.epc.common.business.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/** Cache OAPI Webhook Token */
@Component
public class TokenCache {

  /** openAPI Token Service dependency. */
  @Autowired private TokenService tokenService;

  /**
   * Generate and cache webhook token
   *
   * @return
   */
  @Cacheable(value = WEBHOOK_TOKEN_CACHE, key = "#root.method.name", unless = "#result == null")
  public String getWebhookToken() {
    return tokenService.createWebhookToken();
  }
}
