package com.synkrato.services.partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is useful for products query DTO which will apply the filter on multiple product
 * response
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListQueryDTO {
  private String view;
}
