package com.autoflex.inventory.mapper;

import com.autoflex.inventory.dto.request.ProductRequestDTO;
import com.autoflex.inventory.dto.response.ProductCompositionResponseDTO;
import com.autoflex.inventory.dto.response.ProductResponseDTO;
import com.autoflex.inventory.model.Product;
import com.autoflex.inventory.model.ProductComposition;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductMapper {

  public ProductResponseDTO toResponse(Product entity, List<ProductComposition> compositions) {
    List<ProductCompositionResponseDTO> compDTOs = null;

    if (compositions != null) {
      compDTOs = compositions.stream()
          .map(c -> new ProductCompositionResponseDTO(
              c.getId(),
              c.getProduct().getCode(),
              c.getProduct().getName(),
              c.getRawMaterial().getCode(),
              c.getRawMaterial().getName(),
              c.getQuantityNeeded()))
          .collect(Collectors.toList());
    }

    return new ProductResponseDTO(
        entity.getCode(),
        entity.getName(),
        entity.getPrice(),
        compDTOs);
  }

  public Product toEntity(ProductRequestDTO dto) {
    Product entity = new Product();
    entity.setName(dto.name());
    entity.setPrice(dto.price());
    return entity;
  }
}