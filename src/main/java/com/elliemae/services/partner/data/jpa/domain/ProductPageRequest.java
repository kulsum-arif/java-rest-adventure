package com.synkrato.services.partner.data.jpa.domain;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/* Pageable object for Product */
public class ProductPageRequest extends PageRequest {

  private int offset;

  public ProductPageRequest(int offset, int limit, Sort sort) {
    super(0, limit, sort);
    this.offset = offset;
  }

  /**
   * Get offset to be taken according to the underlying page and page size
   *
   * @return offset
   */
  @Override
  public long getOffset() {
    return this.offset;
  }
}
