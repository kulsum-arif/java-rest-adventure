package com.synkrato.services.partner.enums;

import static com.synkrato.services.partner.controller.BaseController.VIEW_REQUEST_PARAM;

import com.synkrato.components.microservice.exception.ParameterDataInvalidException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum EntityView {
  ID,
  DEFAULT,
  CREDENTIAL,
  INTEGRATIONTYPE,
  ENTITLEMENT,
  WEBHOOK,
  BILLINGRULES,
  OPTIONS,
  COMPLETE,
  EXPORTS,
  RESOURCES,
  FINDINGS,
  SERVICEEVENTS;

  private static final Set<String> entityViewSet;
  private static final Set<String> entityListViewSet;

  static {
    entityViewSet =
        Arrays.stream(EntityView.values()).map(EntityView::name).collect(Collectors.toSet());
  }

  static {
    entityListViewSet =
        Arrays.stream(EntityView.values()).map(EntityView::name).collect(Collectors.toSet());
    entityListViewSet.remove(FINDINGS.name());
    entityListViewSet.remove(SERVICEEVENTS.name());
  }

  /**
   * Returns the Entity View based on the view code.
   *
   * @param viewStr view
   * @return the view corresponding to the code.
   */
  public static EntityView findByCode(String viewStr) {

    if (!entityViewSet.contains(viewStr.toUpperCase())) {
      throw new ParameterDataInvalidException(
          VIEW_REQUEST_PARAM, String.format("Supported views [%s].", String.join(",", entityViewSet)));
    }

    return EntityView.valueOf(viewStr.toUpperCase());
  }

  /**
   * Checks if view is supported for entity list
   * @param viewStr
   * @return
   */
  public static EntityView isSupportedForListView(String viewStr) {

    if (!entityListViewSet.contains(viewStr.toUpperCase())) {
      throw new ParameterDataInvalidException(
          VIEW_REQUEST_PARAM, String.format("Supported views [%s].", String.join(",", entityListViewSet)));
    }

    return EntityView.valueOf(viewStr.toUpperCase());
  }
}
