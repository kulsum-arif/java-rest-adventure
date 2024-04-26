package com.synkrato.services.partner.data;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ux implements Serializable {

  private Boolean showProductOnLenderAdmin;
  private Boolean showOrdersOnLender;
  private Boolean disableReordering;
}
