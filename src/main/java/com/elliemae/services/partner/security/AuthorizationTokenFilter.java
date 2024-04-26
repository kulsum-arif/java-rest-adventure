package com.synkrato.services.partner.security;

import static com.synkrato.services.partner.controller.BaseController.PRODUCT_CONTEXT;
import static com.synkrato.services.partner.controller.BaseController.VERSION;

import com.synkrato.components.microservice.util.EncodeUtils;
import com.synkrato.components.microservice.web.filter.AbstractAuthTokenFilter;
import com.synkrato.services.epc.common.security.SecurityContext;
import com.synkrato.services.epc.common.security.UserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Created by tponnusamy on 2019-03-21 */
@Component
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class AuthorizationTokenFilter extends AbstractAuthTokenFilter {

  private static final String AUTH_HEADER = "Authorization";
  private static final String AUTH_BEARER_PREFIX = "bearer ";

  @Value("${synkrato.epc.filter.auth-audience}")
  private String[] validAudience;

  @Value("${synkrato.epc.filter.required-claims}")
  private String[] requiredClaims;

  @Value("${synkrato.microservice.filter.auth-token.exp-duration-minutes:30}")
  private int expirationDurationMinutes;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    if (request instanceof HttpServletRequest
        && response instanceof HttpServletResponse
        && (((HttpServletRequest) request)
            .getServletPath()
            .startsWith(VERSION + PRODUCT_CONTEXT))) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;

      String token = getAuthToken(httpRequest, httpResponse);
      if (!StringUtils.isEmpty(token)) {
        try {
          Claims claims = getValidClaims(token, httpResponse);
          String encodedToken = Encode.forJava(token);
          log.info("Token received in request={}", EncodeUtils.getMaskedJwt(encodedToken));
          SecurityContext.set(new UserDetails(httpRequest.getHeader(AUTH_HEADER)));

          super.doFilter(request, response, filterChain);
        } finally {
          SecurityContext.unset();
        }
      }
    } else {
      filterChain.doFilter(request, response);
    }
  }

  private String getAuthToken(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
    String token = httpRequest.getHeader(AUTH_HEADER);
    if (StringUtils.isEmpty(token)) {
      setUnauthorizedResponse(httpResponse);
      return null;
    }

    if (token.toLowerCase(Locale.ROOT).startsWith(AUTH_BEARER_PREFIX)) {
      token = token.substring(AUTH_BEARER_PREFIX.length());
    }

    return token;
  }

  private Claims getValidClaims(String token, HttpServletResponse httpResponse) {
    Claims claims = null;
    if (!StringUtils.isEmpty(token)) {
      try {
        claims =
            Jwts.parser()
                .setSigningKey(getSecret(token).getBytes(Charset.defaultCharset()))
                .parseClaimsJws(token)
                .getBody();
      } catch (JwtException e) {
        log.debug("JWT Exception ", e);
        setUnauthorizedResponse(httpResponse);
      }
    }
    return claims;
  }

  @Override
  protected String[] getValidAudiences() {
    return validAudience;
  }

  @Override
  protected String[] getRequiredClaims() {
    return requiredClaims;
  }
}
