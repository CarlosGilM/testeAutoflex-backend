package com.autoflex.inventory.mapper;

import com.autoflex.inventory.dto.request.RawMaterialRequestDTO;
import com.autoflex.inventory.dto.response.RawMaterialResponseDTO;
import com.autoflex.inventory.model.RawMaterial;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RawMaterialMapper {

    public RawMaterialResponseDTO toResponse(RawMaterial entity) {
        return new RawMaterialResponseDTO(
                entity.getCode(),
                entity.getName(),
                entity.getStockQuantity());
    }

    public RawMaterial toEntity(RawMaterialRequestDTO dto) {
        RawMaterial entity = new RawMaterial();
        entity.setName(dto.name());
        entity.setStockQuantity(dto.stockQuantity());
        return entity;
    }
}