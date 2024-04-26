package com.synkrato.services.partner.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("ServiceEvent is optional and part of manifest")
public class ServiceEvent implements Serializable {

  private static final long serialVersionUID = -8130285398770603429L;

  private List<ServiceType> types;
}
