package com.autoflex.inventory.service;

import com.autoflex.inventory.dto.request.ProductCompositionRequestDTO;
import com.autoflex.inventory.dto.response.ProductCompositionResponseDTO;
import com.autoflex.inventory.mapper.ProductCompositionMapper;
import com.autoflex.inventory.model.Product;
import com.autoflex.inventory.model.ProductComposition;
import com.autoflex.inventory.model.RawMaterial;
import com.autoflex.inventory.repository.ProductCompositionRepository;
import com.autoflex.inventory.repository.ProductRepository;
import com.autoflex.inventory.repository.RawMaterialRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductCompositionService {

    @Inject
    ProductCompositionRepository repository;
    @Inject
    ProductRepository productRepository;
    @Inject
    RawMaterialRepository rawMaterialRepository;
    @Inject
    ProductCompositionMapper mapper;

    public List<ProductCompositionResponseDTO> listByProduct(Long productCode) {
        return repository.list("product.code", productCode).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductCompositionResponseDTO addIngredient(ProductCompositionRequestDTO dto) {
        Product product = productRepository.findById(dto.productCode());
        if (product == null)
            throw new NotFoundException("Product with code " + dto.productCode() + " not found");

        RawMaterial rm = rawMaterialRepository.findById(dto.rawMaterialCode());
        if (rm == null)
            throw new NotFoundException("Raw Material with code " + dto.rawMaterialCode() + " not found");

        ProductComposition pc = new ProductComposition();
        pc.setProduct(product);
        pc.setRawMaterial(rm);
        pc.setQuantityNeeded(dto.quantityNeeded());

        repository.persist(pc);
        return mapper.toResponse(pc);
    }

    @Transactional
    public ProductCompositionResponseDTO updateQuantity(Long id, ProductCompositionRequestDTO dto) {
        ProductComposition pc = repository.findById(id);
        if (pc == null)
            throw new NotFoundException("Composition entry not found");

        pc.setQuantityNeeded(dto.quantityNeeded());
        return mapper.toResponse(pc);
    }

    @Transactional
    public void removeIngredient(Long id) {
        boolean deleted = repository.deleteById(id);
        if (!deleted)
            throw new NotFoundException("Composition entry not found");
    }
}