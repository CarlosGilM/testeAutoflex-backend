package com.autoflex.inventory.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponseDTO(
        Long code,
        String name,
        BigDecimal price,
        List<ProductCompositionResponseDTO> compositions) {
}