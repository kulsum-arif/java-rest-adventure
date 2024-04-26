package com.synkrato.services.partner.data;

import com.synkrato.services.epc.common.dto.enums.AdditionalLinkType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalLink implements Serializable {

  private static final long serialVersionUID = -3178751183178167545L;

  private AdditionalLinkType type;

  private String url;

  private String description;

  private String altText;
}
