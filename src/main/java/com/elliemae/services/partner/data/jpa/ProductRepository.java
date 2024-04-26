package com.synkrato.services.partner.data.jpa;

import com.synkrato.services.partner.data.Product;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository
    extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

  List<Product> findByPartnerIdAndName(String partnerId, String name);

  @Query(value = "SELECT '' ~ :regex", nativeQuery = true)
  boolean validatePosixRegex(String regex);

  @EntityGraph(value = "Product.manifest")
  Page<Product> findAll(Specification<Product> specification, Pageable pageable);
}
