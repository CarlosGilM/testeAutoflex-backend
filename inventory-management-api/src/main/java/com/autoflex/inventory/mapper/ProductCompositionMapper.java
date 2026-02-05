package com.autoflex.inventory.mapper;

import com.autoflex.inventory.dto.response.ProductCompositionResponseDTO;
import com.autoflex.inventory.model.ProductComposition;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductCompositionMapper {

    public ProductCompositionResponseDTO toResponse(ProductComposition entity) {
        if (entity == null) {
            return null;
        }

        return new ProductCompositionResponseDTO(
                entity.getId(),
                entity.getProduct().getCode(),
                entity.getProduct().getName(),
                entity.getRawMaterial().getCode(),
                entity.getRawMaterial().getName(),
                entity.getQuantityNeeded());
    }
}