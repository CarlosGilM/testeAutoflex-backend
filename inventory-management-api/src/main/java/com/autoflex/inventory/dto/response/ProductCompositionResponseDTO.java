package com.autoflex.inventory.dto.response;

public record ProductCompositionResponseDTO(
        Long id,
        Long productCode,
        String productName,
        Long rawMaterialCode,
        String rawMaterialName,
        Double quantityNeeded) {
}