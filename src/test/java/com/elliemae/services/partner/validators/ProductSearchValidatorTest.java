package com.synkrato.services.partner.validators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.synkrato.components.microservice.exception.ParameterDataInvalidException;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import com.synkrato.services.partner.util.TestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProductSearchValidatorTest {

  @InjectMocks ProductSearchValidator productSearchValidator;
  @Mock MessageUtil messageUtil;

  @Test
  public void noFieldsTest() {

    // Set up
    ProductSearchDTO productSearchDTO = ProductSearchDTO.builder().build();
    Errors errors = new BindException(productSearchDTO, "productSearchObject");
    TestHelper.buildInternalAdminJwt();

    // Test
    ValidationUtils.invokeValidator(productSearchValidator, productSearchDTO, errors);

    // Assert
    assertEquals(0, errors.getFieldErrors().size());
  }

  @Test
  public void invalidFieldsTest() {

    // Set up
    ProductSearchDTO productSearchDTO =
        ProductSearchDTO.builder()
            .partnerId("0000")
            .start("invalid-start")
            .limit("invalid-limit")
            .build();
    Errors errors = new BindException(productSearchDTO, "productSearchObject");
    TestHelper.buildInternalAdminJwt();

    // Test
    ValidationUtils.invokeValidator(productSearchValidator, productSearchDTO, errors);

    // Assert
    assertEquals(2, errors.getFieldErrors().size());
    assertEquals(
        "synkrato.services.epc.common.search-attribute.start.invalid",
        errors.getAllErrors().get(0).getCode());
    assertEquals(
        "synkrato.services.epc.common.search-attribute.limit.invalid",
        errors.getAllErrors().get(1).getCode());
  }

  @Test
  public void maxLimitTest() {

    // Setup
    ReflectionTestUtils.setField(productSearchValidator, "maxQueryLimit", 10);

    ProductSearchDTO productSearchDTO = ProductSearchDTO.builder().limit("99").build();
    Errors errors = new BindException(productSearchDTO, "productSearchObject");
    TestHelper.buildInternalAdminJwt();

    // Test
    ValidationUtils.invokeValidator(productSearchValidator, productSearchDTO, errors);

    // Assert
    assertEquals(1, errors.getFieldErrors().size());
    assertEquals(
        "synkrato.services.epc.common.search-attribute.limit.invalid",
        errors.getAllErrors().get(0).getCode());
  }

  @Test
  public void invalidStatusTest() {
    boolean hasException = false;

    // Setup
    ProductSearchDTO productSearchDTO = ProductSearchDTO.builder().status("").build();
    Errors errors = new BindException(productSearchDTO, "productSearchObject");
    TestHelper.buildInternalAdminJwt();

    // Test
    try {
      ValidationUtils.invokeValidator(productSearchValidator, productSearchDTO, errors);

    } catch (ParameterDataInvalidException e) {
      assertEquals(
          "Invalid data for parameter(s) 'status' (Supported status [development,approved,deprecated,inreview].)",
          e.getMessage());
      hasException = true;
    }

    assertTrue(hasException);
  }

  @Test
  public void emptyStatusTest() {
    boolean hasException = false;

    // Setup
    ProductSearchDTO productSearchDTO = ProductSearchDTO.builder().status("invalid-status").build();
    Errors errors = new BindException(productSearchDTO, "productSearchObject");
    TestHelper.buildInternalAdminJwt();

    // Test
    try {
      ValidationUtils.invokeValidator(productSearchValidator, productSearchDTO, errors);

    } catch (ParameterDataInvalidException e) {
      assertEquals(
          "Invalid data for parameter(s) 'status' (Supported status [development,approved,deprecated,inreview].)",
          e.getMessage());
      hasException = true;
    }

    assertTrue(hasException);
  }

  @Test(expected = EpcRuntimeException.class)
  public void invalidPartnerIdTest() {
    // Setup
    ProductSearchDTO productSearchDTO =
        ProductSearchDTO.builder().partnerId("invalid-partner-id").build();
    Errors errors = new BindException(productSearchDTO, "productSearchObject");
    TestHelper.buildPartnerJWT();

    // Test
    ValidationUtils.invokeValidator(productSearchValidator, productSearchDTO, errors);
  }
}
