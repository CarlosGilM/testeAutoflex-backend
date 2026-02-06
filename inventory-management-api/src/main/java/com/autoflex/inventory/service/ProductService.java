package com.autoflex.inventory.service;

import com.autoflex.inventory.dto.request.ProductCompositionRequestDTO;
import com.autoflex.inventory.dto.request.ProductRequestDTO;
import com.autoflex.inventory.dto.response.ProductResponseDTO;
import com.autoflex.inventory.dto.response.ProductionSuggestionResponseDTO;
import com.autoflex.inventory.mapper.ProductMapper;
import com.autoflex.inventory.model.Product;
import com.autoflex.inventory.model.ProductComposition;
import com.autoflex.inventory.model.RawMaterial;
import com.autoflex.inventory.repository.ProductCompositionRepository;
import com.autoflex.inventory.repository.ProductRepository;
import com.autoflex.inventory.repository.RawMaterialRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.Map;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductService {

  @Inject
  ProductRepository repository;
  @Inject
  ProductMapper mapper;
  @Inject
  RawMaterialRepository rawMaterialRepository;
  @Inject
  ProductCompositionRepository compositionRepository;

  public List<ProductResponseDTO> listAll() {
    return repository.listAll().stream()
        .map(p -> {
          List<ProductComposition> comps = compositionRepository.list("product.code", p.getCode());
          return mapper.toResponse(p, comps);
        })
        .collect(Collectors.toList());
  }

  public ProductResponseDTO getByCode(Long code) {
    Product product = repository.findById(code);
    if (product == null)
      throw new NotFoundException("Product not found");

    List<ProductComposition> comps = compositionRepository.list("product.code", code);
    return mapper.toResponse(product, comps);
  }

  @Transactional
  public ProductResponseDTO create(ProductRequestDTO dto) {
    if (dto.compositions() == null || dto.compositions().isEmpty()) {
      throw new BadRequestException("O produto deve ter no mínimo uma matéria-prima na receita.");
    }

    Product product = mapper.toEntity(dto);
    repository.persist(product);

    for (ProductCompositionRequestDTO compDto : dto.compositions()) {
      RawMaterial rm = rawMaterialRepository.findById(compDto.rawMaterialCode());
      if (rm == null)
        throw new NotFoundException("Raw Material not found: " + compDto.rawMaterialCode());

      ProductComposition pc = new ProductComposition();
      pc.setProduct(product);
      pc.setRawMaterial(rm);
      pc.setQuantityNeeded(compDto.quantityNeeded());
      compositionRepository.persist(pc);
    }

    List<ProductComposition> comps = compositionRepository.list("product.code", product.getCode());
    return mapper.toResponse(product, comps);
  }

  @Transactional
  public ProductResponseDTO update(Long code, ProductRequestDTO dto) {
    Product product = repository.findById(code);
    if (product == null)
      throw new NotFoundException("Product not found");

    product.setName(dto.name());
    product.setPrice(dto.price());

    List<ProductComposition> comps = compositionRepository.list("product.code", code);
    return mapper.toResponse(product, comps);
  }

  @Transactional
  public void delete(Long code) {
    if (repository.findById(code) == null)
      throw new NotFoundException("Product not found");

    compositionRepository.delete("product.code", code);
    repository.deleteById(code);
  }

  public List<ProductionSuggestionResponseDTO> getProductionSuggestion() {

    Map<Long, Double> currentStock = rawMaterialRepository.listAll()
        .stream()
        .collect(Collectors.toMap(RawMaterial::getCode, RawMaterial::getStockQuantity));

    // Busca todos os produtos ordenados
    List<Product> products = repository.listAllOrderedByPriceDesc();

    // Busca todos as composições de uma vez agrupa por ID do Produto.
    Map<Long, List<ProductComposition>> compositionsMap = compositionRepository.listAll()
        .stream()
        .collect(Collectors.groupingBy(c -> c.getProduct().getCode()));

    List<ProductionSuggestionResponseDTO> suggestions = new ArrayList<>();

    for (Product product : products) {
      List<ProductComposition> compositions = compositionsMap.get(product.getCode());

      if (compositions == null || compositions.isEmpty()) {
        continue;
      }

      int maxQuantity = Integer.MAX_VALUE;
      for (ProductComposition comp : compositions) {
        double needed = comp.getQuantityNeeded();

        double available = currentStock.getOrDefault(comp.getRawMaterial().getCode(), 0.0);
        int possible = (int) (available / needed);

        if (possible < maxQuantity) {
          maxQuantity = possible;
        }
      }

      if (maxQuantity == Integer.MAX_VALUE)
        maxQuantity = 0;

      if (maxQuantity > 0) {
        // Tira do estoque virtual
        for (ProductComposition comp : compositions) {
          Long rmCode = comp.getRawMaterial().getCode();
          double consumed = comp.getQuantityNeeded() * maxQuantity;
          currentStock.put(rmCode, currentStock.get(rmCode) - consumed);
        }

        suggestions.add(new ProductionSuggestionResponseDTO(
            product.getCode(),
            product.getName(),
            product.getPrice(),
            maxQuantity,
            product.getPrice().multiply(BigDecimal.valueOf(maxQuantity))));
      }
    }

    return suggestions;
  }
}