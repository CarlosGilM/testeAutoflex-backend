package com.autoflex.inventory.dto.response;

import java.math.BigDecimal;

public record ProductionSuggestionResponseDTO(
        Long productCode,
        String productName,
        BigDecimal productPrice,
        Integer quantityToProduce,
        BigDecimal totalEstimatedValue) {
}