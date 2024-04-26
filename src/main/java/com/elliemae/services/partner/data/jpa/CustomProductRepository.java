package com.synkrato.services.partner.data.jpa;

import com.synkrato.services.partner.data.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

/**
 * This is a custom repository to override get products methods.
 *
 * @author rarora2
 */
@Repository
@FunctionalInterface
public interface CustomProductRepository {

  Page<Product> findAll(Specification<Product> specification, Pageable pageable);
}
