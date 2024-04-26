package com.synkrato.services.partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** This class is useful for product query DTO which will apply the filter on product response */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductQueryDTO {
  private String view;
}
