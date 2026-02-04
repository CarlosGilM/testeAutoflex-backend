package com.autoflex.inventory.dto.response;

public record RawMaterialResponseDTO(
        Long code,
        String name,
        Double stockQuantity) {
}