package com.autoflex.inventory.dto.request;

public record ProductCompositionRequestDTO(
        Long productCode,
        Long rawMaterialCode,
        Double quantityNeeded) {
}