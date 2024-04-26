package com.synkrato.services.partner.util;

import com.synkrato.components.microservice.identity.IdentityContext;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.partner.data.AccessEntitlement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Utility class for access entitlement related functions.
 *
 * @author rarora2
 */
@Component
public class AccessEntitlementUtil {

  /**
   * Checks authorization for an requesting tenant against the access entitlement configured for the
   * product.
   *
   * @param accessEntitlement allow and deny list of patterns
   * @return authorized or not
   */
  public boolean isTenantAuthorized(AccessEntitlement accessEntitlement) {
    return CommonUtil.hasAnyLenderToken()
        && !isMatchPattern(accessEntitlement.getDeny(), IdentityContext.get().getRealm())
        && isMatchPattern(accessEntitlement.getAllow(), IdentityContext.get().getRealm());
  }

  /**
   * Sanitizes the incoming list of regex patterns into a single pattern joined by |.
   *
   * @param patternList list of deny/allow regex pattern from request
   * @return combined single pattern list
   */
  public String getPatternString(List<String> patternList) {
    patternList = patternList.parallelStream().map(s -> "^" + s + "$").collect(Collectors.toList());

    return String.join("|", patternList);
  }

  /**
   * Converts the pattern string to a list of patterns delimited by |.
   *
   * @param pattern incoming pattern combined
   * @return list of patterns
   */
  public List<String> getPatternList(String pattern) {

    return Arrays.stream(pattern.split("\\|"))
        .map(p -> p.substring(1, p.length() - 1))
        .collect(Collectors.toList());
  }

  /**
   * Tests an input value against combined regex patterns delimited by |.
   *
   * @param pattern allow/deny pattern obtained from database
   * @param input input value, ie, tenant
   * @return matched or not
   */
  private boolean isMatchPattern(String pattern, String input) {

    // Pattern matching is case-insensitive by forcing lowercase for both
    return input.toLowerCase().matches(pattern.toLowerCase());
  }
}
