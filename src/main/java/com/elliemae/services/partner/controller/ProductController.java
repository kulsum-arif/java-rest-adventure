package com.synkrato.services.partner.controller;

import static com.synkrato.services.epc.common.EpcCommonConstants.APPLICATION_MERGE_PATCH_JSON_TYPE;
import static com.synkrato.services.epc.common.EpcCommonConstants.ID_VIEW;
import static com.synkrato.services.epc.common.EpcCommonConstants.LIMIT;
import static com.synkrato.services.epc.common.EpcCommonConstants.NAME_ATTRIBUTE;
import static com.synkrato.services.epc.common.EpcCommonConstants.REQUEST_TYPE;
import static com.synkrato.services.epc.common.EpcCommonConstants.START;
import static com.synkrato.services.epc.common.EpcCommonConstants.VIEW_CREDENTIAL;
import static com.synkrato.services.epc.common.EpcCommonConstants.VIEW_DEFAULT;
import static com.synkrato.services.partner.PartnerServiceConstants.APPLICATIONS;
import static com.synkrato.services.partner.PartnerServiceConstants.CATEGORIES;
import static com.synkrato.services.partner.PartnerServiceConstants.WORKFLOWS;
import static com.synkrato.services.partner.controller.BaseController.PRODUCT_CONTEXT;
import static com.synkrato.services.partner.controller.BaseController.VERSION;

import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.marshaller.EpcMarshaller;
import com.synkrato.services.epc.common.validator.ProductCreateGroup;
import com.synkrato.services.partner.SwaggerConstants;
import com.synkrato.services.partner.business.ProductService;
import com.synkrato.services.partner.dto.ProductListQueryDTO;
import com.synkrato.services.partner.dto.ProductQueryDTO;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import com.synkrato.services.partner.enums.Permission;
import com.synkrato.services.partner.security.Authorization;
import com.synkrato.services.partner.util.ProductUtil;
import com.synkrato.services.partner.validators.ProductCreateValidator;
import com.synkrato.services.partner.validators.ProductListQueryValidator;
import com.synkrato.services.partner.validators.ProductQueryValidator;
import com.synkrato.services.partner.validators.ProductSearchValidator;
import com.synkrato.services.partner.validators.ProductValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(
    tags = SwaggerConstants.PRODUCT_CONTROLLER_SWAGGER_TAG,
    value = SwaggerConstants.PRODUCT_CONTROLLER_SWAGGER_VALUE)
@RestController
@RequestMapping(VERSION + PRODUCT_CONTEXT)
public class ProductController extends BaseController {

  private static final String VIEW =
      VIEW_REQUEST_PARAM_ID
          + ","
          + VIEW_DEFAULT
          + ","
          + VIEW_CREDENTIAL
          + ","
          + VIEW_REQUEST_PARAM_INTEGRATION_TYPE
          + ","
          + VIEW_REQUEST_PARAM_ENTITLEMENT
          + ","
          + VIEW_REQUEST_PARAM_WEBHOOK
          + ","
          + VIEW_RESOURCES
          + ","
          + VIEW_REQUEST_PARAM_COMPLETE;

  private static final String PRODUCT_BY_ID_VIEW =
      VIEW + "," + VIEW_REQUEST_PARAM_FINDINGS + "," + VIEW_REQUEST_PARAM_SERVICEEVENTS;

  @Autowired private EpcMarshaller<ProductDTO> epcMarshaller;
  @Autowired private EpcMarshaller<ProductSearchDTO> productSearchMarshaller;
  @Autowired private ProductService productService;
  @Autowired private ProductCreateValidator productCreateValidator;
  @Autowired private ProductValidator productValidator;
  @Autowired private ProductSearchValidator productSearchValidator;
  @Autowired private ProductUtil productUtil;
  @Autowired private ProductQueryValidator productQueryValidator;
  @Autowired private ProductListQueryValidator productListQueryValidator;

  @ApiOperation(
      value = SwaggerConstants.PRODUCT_OPERATION_CREATE_SWAGGER_SUMMARY,
      notes = SwaggerConstants.PRODUCT_OPERATION_CREATE_SWAGGER_DESCRIPTION,
      nickname = "createProductUsingPOST")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = VIEW_REQUEST_PARAM,
        value = "View",
        dataTypeClass = String.class,
        paramType = "query",
        allowableValues = PRODUCT_BY_ID_VIEW)
  })
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ProductDTO.class))
      })
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Authorization(permission = Permission.CREATE_PRODUCT)
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<Object> createProduct(
      @RequestBody @Validated(ProductCreateGroup.class) @Valid ProductDTO product,
      @Validated ProductQueryDTO productQueryDTO,
      HttpServletRequest request) {

    productUtil.buildProductQueryView(productQueryDTO, ID_VIEW);
    ProductDTO newProduct = productService.create(product, productQueryDTO.getView());

    URI location = getResourceURI(request, newProduct.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .location(location)
        .body(resolveView(newProduct, getEntityView(productQueryDTO.getView())));
  }

  @ApiOperation(
      value = SwaggerConstants.PRODUCT_OPERATION_UPDATE_SWAGGER_SUMMARY,
      notes = SwaggerConstants.PRODUCT_OPERATION_UPDATE_SWAGGER_DESCRIPTION,
      nickname = "updateProductUsingPATCH")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ProductDTO.class))
      })
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = VIEW_REQUEST_PARAM,
        value = "View",
        dataTypeClass = String.class,
        paramType = "query",
        allowableValues = PRODUCT_BY_ID_VIEW)
  })
  @PatchMapping(
      consumes = APPLICATION_MERGE_PATCH_JSON_TYPE,
      produces = MediaType.APPLICATION_JSON_VALUE,
      value = PRODUCT_ID_PATH_PARAM_MAPPING)
  @Authorization(permission = Permission.UPDATE_PRODUCT)
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<Object> updateProduct(
      @PathVariable(value = PRODUCT_ID_PATH_PARAM) String id,
      @RequestBody Map<String, Object> updateProductMap,
      @Validated ProductQueryDTO productQueryDTO)
      throws MethodArgumentNotValidException {

    productUtil.buildProductQueryView(productQueryDTO, ID_VIEW);
    ProductDTO mergedProductDto =
        productService.update(id, updateProductMap, productQueryDTO.getView());

    return ResponseEntity.status(HttpStatus.OK)
        .body(resolveView(mergedProductDto, getEntityView(productQueryDTO.getView())));
  }

  @ApiOperation(
      value = SwaggerConstants.PRODUCT_OPERATION_GET_ALL_SWAGGER_SUMMARY,
      notes = SwaggerConstants.PRODUCT_OPERATION_GET_ALL_SWAGGER_DESCRIPTION,
      nickname = "findAllProductUsingGET",
      response = ProductDTO.class,
      responseContainer = "List")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = PARTNER_ID_PARAM,
        value = "Partner id of the service provider",
        dataTypeClass = String.class,
        paramType = "query"),
    @ApiImplicitParam(
        name = NAME_ATTRIBUTE,
        value = "Product name of the service provider",
        dataTypeClass = String.class,
        paramType = "query"),
    @ApiImplicitParam(
        name = CATEGORIES,
        value = "Comma-separated list of categories associated with the product",
        dataTypeClass = String.class,
        paramType = "query"),
    @ApiImplicitParam(
        name = REQUEST_TYPE,
        value = "request type associated with the product",
        dataTypeClass = String.class,
        paramType = "query"),
    @ApiImplicitParam(
        name = APPLICATIONS,
        value = "Comma-separated list of applications associated with the product",
        dataTypeClass = String.class,
        paramType = "query"),
    @ApiImplicitParam(
        name = WORKFLOWS,
        value = "Comma-separated list of workflows associated with the product",
        dataTypeClass = String.class,
        paramType = "query"),
    @ApiImplicitParam(
        name = START,
        value = "Start index of the product to fetch",
        dataTypeClass = String.class,
        paramType = "query"),
    @ApiImplicitParam(
        name = LIMIT,
        value = "Total number of products to fetch. Default value is 25.",
        dataTypeClass = String.class,
        paramType = "query"),
    @ApiImplicitParam(
        name = "status",
        value = "Product status",
        dataTypeClass = String.class,
        paramType = "query",
        allowableValues = "development, inreview, approved, deprecated"),
    @ApiImplicitParam(
        name = VIEW_REQUEST_PARAM,
        value = "Product view",
        dataTypeClass = String.class,
        paramType = "query",
        allowableValues = VIEW)
  })
  @Authorization(permission = Permission.GET_PRODUCT)
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findAll(
      @ApiParam(hidden = true) @RequestParam Map<String, Object> productSearchMap,
      @Validated ProductListQueryDTO productListQueryDTO,
      HttpServletResponse response)
      throws MethodArgumentNotValidException {

    productUtil.buildProductListQueryView(productListQueryDTO);
    ProductSearchDTO productSearch = initProductSearch(productSearchMap);

    // TODO Replace Credential view with default view once consumers are ready to get credentials
    // metadata by calling get product by id
    List<ProductDTO> productList =
        productService.findAll(productSearch, productListQueryDTO.getView());

    addResponseHeader(
        response, RESPONSE_HEADER_ITEM_COUNT, String.valueOf(productSearch.getTotalProductCount()));

    return new ResponseEntity(
        resolveListViewProduct(productList, getEntityView(productListQueryDTO.getView())),
        HttpStatus.OK);
  }

  @ApiOperation(
      value = SwaggerConstants.PRODUCT_OPERATION_GET_SPECIFIC_SWAGGER_SUMMARY,
      notes = SwaggerConstants.PRODUCT_OPERATION_GET_SPECIFIC_SWAGGER_DESCRIPTION,
      nickname = "findProductByIdUsingGET")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ProductDTO.class))
      })
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = VIEW_REQUEST_PARAM,
        value = "View",
        dataTypeClass = String.class,
        paramType = "query",
        allowableValues = PRODUCT_BY_ID_VIEW)
  })
  @Authorization(permission = Permission.GET_PRODUCT)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = PRODUCT_ID_PATH_PARAM_MAPPING)
  public ResponseEntity<Object> findById(
      @PathVariable(value = PRODUCT_ID_PATH_PARAM) String id,
      @Validated ProductQueryDTO productQueryDTO) {

    productUtil.buildProductQueryView(productQueryDTO, VIEW_DEFAULT);

    return new ResponseEntity<>(
        resolveView(
            productService.findById(id, productQueryDTO.getView()),
            getEntityView(productQueryDTO.getView())),
        HttpStatus.OK);
  }

  @InitBinder(PRODUCT_DTO)
  void initBinderProductCreate(final ServletRequestDataBinder binder) {
    binder.addValidators(productCreateValidator);
  }

  @InitBinder(PRODUCT_SEARCH_ATTRIBUTE)
  void initBinderProductSearch(final ServletRequestDataBinder binder) {
    binder.addValidators(productSearchValidator);
  }

  @InitBinder(PRODUCT_QUERY_DTO)
  void initBinderProductQuery(final ServletRequestDataBinder binder) {
    binder.addValidators(productQueryValidator);
  }

  @InitBinder("productListQueryDTO")
  void initBinderAllProductsQuery(final ServletRequestDataBinder binder) {
    binder.addValidators(productListQueryValidator);
  }

  /**
   * Initialize Product Search Request
   *
   * @param productSearchMap Product Search Map containing query params
   * @return Product Search DTO
   */
  private ProductSearchDTO initProductSearch(Map<String, Object> productSearchMap)
      throws MethodArgumentNotValidException {

    ProductSearchDTO productSearch =
        productSearchMarshaller.convertValue(productSearchMap, ProductSearchDTO.class);
    productSearch.setTags(productUtil.buildProductTagQueryParams(productSearchMap));
    BindingResult bindingResults =
        new BindException(productSearch, ProductSearchDTO.class.getSimpleName());
    ValidationUtils.invokeValidator(productSearchValidator, productSearch, bindingResults);
    if (bindingResults.hasErrors()) {
      MethodParameter methodParameter =
          new MethodParameter(new Object() {}.getClass().getEnclosingMethod(), 0);
      productValidator.handleErrors(methodParameter, bindingResults);
    }

    return productSearch;
  }
}
