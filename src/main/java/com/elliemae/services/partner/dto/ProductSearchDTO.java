package com.synkrato.services.partner.dto;

import static com.synkrato.services.epc.common.EpcCommonConstants.ENVIRONMENT;
import static com.synkrato.services.epc.common.EpcCommonConstants.NAME_ATTRIBUTE;
import static com.synkrato.services.epc.common.EpcCommonConstants.PARTNER_ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.REQUEST_TYPE;
import static com.synkrato.services.epc.common.EpcCommonConstants.STATUS;
import static com.synkrato.services.partner.PartnerServiceConstants.EXTENSION_LIMIT;
import static com.synkrato.services.partner.PartnerServiceConstants.LISTING_NAME;
import static com.synkrato.services.partner.PartnerServiceConstants.TAG_ATTRIBUTE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("Product Search Attributes")
public class ProductSearchDTO implements Serializable {

  private static final long serialVersionUID = 5789977172935199739L;

  public static final List<String> queryableAttributes =
      Arrays.asList(
          ENVIRONMENT,
          NAME_ATTRIBUTE,
          PARTNER_ID,
          TAG_ATTRIBUTE,
          EXTENSION_LIMIT,
          STATUS,
          REQUEST_TYPE); // NOSONAR

  public static final List<String> orderByAttributes =
      Arrays.asList(LISTING_NAME, "-" + LISTING_NAME); // NOSONAR

  @ApiModelProperty(notes = "Partner id of the service provider")
  private String partnerId;

  @ApiModelProperty(notes = "Product name of the service provider")
  private String name;

  @ApiModelProperty(notes = "Query start index")
  private String start;

  @ApiModelProperty(notes = "Total number of products to fetch")
  private String limit;

  @ApiModelProperty(notes = "Sort attributes")
  private String sort;

  @ApiModelProperty(notes = "Has PLM extensions or not")
  private Boolean extensions;

  @ApiModelProperty(notes = "Product status")
  private String status;

  @ApiModelProperty(notes = "Product request type")
  private String requestType;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private String tenant;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private String environment;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private Map<String, List<String>> tags;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private Map<String, Object> whereClause;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private List<String> orderByClause;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private String tagCriteria;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private String requestTypesCriteria;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private long totalProductCount;
}
