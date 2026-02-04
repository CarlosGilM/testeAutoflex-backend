package com.autoflex.inventory.dto.request;

public record RawMaterialRequestDTO(
    String name,
    Double stockQuantity) {
}