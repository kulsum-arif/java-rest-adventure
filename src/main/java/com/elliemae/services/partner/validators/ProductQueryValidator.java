package com.synkrato.services.partner.validators;

import com.synkrato.components.microservice.exception.ParameterDataInvalidException;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.partner.dto.ProductQueryDTO;
import com.synkrato.services.partner.enums.EntityView;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@Slf4j
public class ProductQueryValidator implements Validator {

  @Override
  public boolean supports(Class<?> aClass) {
    return ProductQueryDTO.class.equals(aClass);
  }

  @Override
  public void validate(Object productQueryDTO, Errors errors) {

    ProductQueryDTO productQueryDTO1 = (ProductQueryDTO) productQueryDTO;

    // Biz dev user is allowed to query the product only with default view
    if (StringUtils.isNotEmpty(productQueryDTO1.getView())) {
      if (CommonUtil.isBizDevUser()
          && !CommonUtil.hasInternalAdminToken()
          && !productQueryDTO1.getView().equalsIgnoreCase(EntityView.DEFAULT.name())) {
        throw new ParameterDataInvalidException("view", "Supported views [DEFAULT].");
      }
      EntityView.findByCode(productQueryDTO1.getView());
    }
  }
}
