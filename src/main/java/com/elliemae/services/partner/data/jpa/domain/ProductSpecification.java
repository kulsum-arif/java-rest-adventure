package com.synkrato.services.partner.data.jpa.domain;

import static com.synkrato.services.epc.common.EpcCommonConstants.ENVIRONMENT;
import static com.synkrato.services.partner.PartnerServiceConstants.EXTENSION_LIMIT;
import static com.synkrato.services.partner.PartnerServiceConstants.REQUEST_TYPES_ATTRIBUTE;
import static com.synkrato.services.partner.PartnerServiceConstants.TAG_ATTRIBUTE;
import static com.synkrato.services.partner.config.CustomDialect.CUSTOM_JSONB_CONTAINS;

import com.synkrato.services.epc.common.dto.enums.EnvironmentType;
import com.synkrato.services.partner.data.Product;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Setter
@Slf4j
public class ProductSpecification implements Specification<Product> {

  private Map<String, Object> whereClauseFields;
  private String tagCriteria;
  private String requestTypesCriteria;

  @Override
  public Predicate toPredicate(
      Root<Product> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

    final Collection<Predicate> predicates = new ArrayList<>();

    if (!CollectionUtils.isEmpty(whereClauseFields)) {
      predicates.add(buildConjunctionPredicate(whereClauseFields, root, criteriaBuilder));
      predicates.add(buildEnvironmentPredicate(whereClauseFields, root, criteriaBuilder));
    }

    if (!ObjectUtils.isEmpty(tagCriteria)) {
      predicates.add(
          buildCustomJsonbContainsPredicate(tagCriteria, root, criteriaBuilder, TAG_ATTRIBUTE));
    }

    if (!ObjectUtils.isEmpty(requestTypesCriteria)) {
      predicates.add(
          buildCustomJsonbContainsPredicate(
              requestTypesCriteria, root, criteriaBuilder, REQUEST_TYPES_ATTRIBUTE));
    }

    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
  }

  private Predicate buildCustomJsonbContainsPredicate(
      String criteria, Root<Product> root, CriteriaBuilder criteriaBuilder, String columnName) {

    Predicate predicate = criteriaBuilder.conjunction();

    predicate
        .getExpressions()
        .add(
            criteriaBuilder.equal(
                criteriaBuilder.function(
                    CUSTOM_JSONB_CONTAINS,
                    Boolean.class,
                    root.get(columnName),
                    criteriaBuilder.literal(criteria)),
                true));

    return predicate;
  }

  /**
   * Build AND Conjunction predicate
   *
   * @param whereClauseFields
   * @param root
   * @param criteriaBuilder
   * @return
   */
  public Predicate buildConjunctionPredicate(
      Map<String, Object> whereClauseFields, Root<?> root, CriteriaBuilder criteriaBuilder) {

    Predicate predicate = criteriaBuilder.conjunction();

    whereClauseFields.forEach(
        (key, value) -> {
          if (ProductSearchDTO.queryableAttributes.contains(key) && !key.equals(ENVIRONMENT)) {
            switch (key) {
              case EXTENSION_LIMIT:
                predicate
                    .getExpressions()
                    .add(
                        (boolean) value
                            ? criteriaBuilder.greaterThan(root.get(key), 0)
                            : criteriaBuilder.equal(root.get(key), 0));
                break;
              default:
                predicate.getExpressions().add(criteriaBuilder.equal(root.get(key), value));
            }
          }
        });

    return predicate;
  }

  /**
   * Always get prod products regardless of environment
   *
   * @param whereClauseFields
   * @param root
   * @param criteriaBuilder
   * @return
   */
  public Predicate buildEnvironmentPredicate(
      Map<String, Object> whereClauseFields, Root<?> root, CriteriaBuilder criteriaBuilder) {

    Predicate predicate;
    if (EnvironmentType.prod.name().equals(whereClauseFields.get(ENVIRONMENT))) {
      predicate = criteriaBuilder.conjunction();
    } else {
      predicate = criteriaBuilder.disjunction();
      predicate
          .getExpressions()
          .add(criteriaBuilder.equal(root.get(ENVIRONMENT), whereClauseFields.get(ENVIRONMENT)));
    }
    predicate
        .getExpressions()
        .add(criteriaBuilder.equal(root.get(ENVIRONMENT), EnvironmentType.prod.name()));

    return predicate;
  }
}
