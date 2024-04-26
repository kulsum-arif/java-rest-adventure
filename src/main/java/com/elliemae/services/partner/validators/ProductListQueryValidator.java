package com.synkrato.services.partner.validators;

import static com.synkrato.services.partner.controller.BaseController.VIEW_REQUEST_PARAM;

import com.synkrato.components.microservice.exception.ParameterDataInvalidException;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.partner.dto.ProductListQueryDTO;
import com.synkrato.services.partner.enums.EntityView;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@Slf4j
public class ProductListQueryValidator implements Validator {

  @Override
  public boolean supports(Class<?> aClass) {
    return ProductListQueryDTO.class.equals(aClass);
  }

  @Override
  public void validate(Object productListQuery, Errors errors) {

    ProductListQueryDTO productQueryDTO = (ProductListQueryDTO) productListQuery;
    if (ObjectUtils.isEmpty(productQueryDTO.getView())) {
      return;
    }

    // Biz dev user is allowed to query the products only with default view
    if (CommonUtil.isBizDevUser()
        && !CommonUtil.hasInternalAdminToken()
        && !productQueryDTO.getView().equalsIgnoreCase(EntityView.DEFAULT.name())) {
      throw new ParameterDataInvalidException(VIEW_REQUEST_PARAM, "Supported views [DEFAULT].");
    }
    EntityView.isSupportedForListView(productQueryDTO.getView());
  }
}
