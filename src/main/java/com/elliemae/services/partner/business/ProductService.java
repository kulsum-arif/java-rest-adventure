package com.synkrato.services.partner.business;

import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Service
public interface ProductService {

  List<ProductDTO> findAll(ProductSearchDTO productSearch, String view);

  List<ProductDTO> findByPartnerIdAndName(String partnerId, String name);

  ProductDTO findById(String id, String view);

  ProductDTO create(ProductDTO productDTO, String view);

  ProductDTO update(String id, Map<String, Object> updateProductMap, String view)
      throws MethodArgumentNotValidException;

  boolean validateRegex(String regex);
}
