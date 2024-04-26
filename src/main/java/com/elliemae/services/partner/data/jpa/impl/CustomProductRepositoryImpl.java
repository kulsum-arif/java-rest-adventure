package com.synkrato.services.partner.data.jpa.impl;

import static com.synkrato.services.partner.PartnerServiceConstants.ACCESS_FILTER_NAME;
import static com.synkrato.services.partner.PartnerServiceConstants.ACCESS_FILTER_PARAM_TENANT_ID;

import com.synkrato.components.microservice.identity.IdentityContext;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.partner.data.Product;
import com.synkrato.services.partner.data.jpa.CustomProductRepository;
import com.synkrato.services.partner.data.jpa.ProductRepository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

/**
 * This is a custom repository to override get products methods. This enables access entitlement and
 * exclude deprecated product filters for current transaction based on the condition.
 *
 * @author rarora2
 */
@Repository
public class CustomProductRepositoryImpl implements CustomProductRepository {

  @Autowired private ProductRepository productRepository;
  @PersistenceContext private EntityManager entityManager;

  /**
   * Override the existing findAll and enable access entitlement and exclude deprecated product
   * filer for current transaction.
   *
   * @param specification product specification
   * @param pageable pagination object to achieve pagination
   * @return found products
   */
  @Override
  public Page<Product> findAll(Specification<Product> specification, Pageable pageable) {
    enableFilter();
    Page<Product> result = productRepository.findAll(specification, pageable);
    disableFilter();

    return result;
  }

  /**
   * Enable the access entitlement and exclude deprecated product filter for current transaction.
   */
  private void enableFilter() {
    if (CommonUtil.hasLenderOrApplicationToken()) {
      entityManager
          .unwrap(Session.class)
          .enableFilter(ACCESS_FILTER_NAME)
          .setParameter(
              ACCESS_FILTER_PARAM_TENANT_ID, IdentityContext.get().getRealm().toLowerCase());
    }
  }

  /**
   * Disable the access entitlement and exclude deprecated product filter for current transaction.
   */
  private void disableFilter() {
    if (CommonUtil.hasLenderOrApplicationToken()) {
      entityManager.unwrap(Session.class).disableFilter(ACCESS_FILTER_NAME);
    }
  }
}
