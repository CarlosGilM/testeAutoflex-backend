package com.autoflex.inventory.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequestDTO(
                String name,
                BigDecimal price,
                List<ProductCompositionRequestDTO> compositions) {
}