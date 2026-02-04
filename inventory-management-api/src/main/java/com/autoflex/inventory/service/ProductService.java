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
    Product product = mapper.toEntity(dto);
    repository.persist(product);

    if (dto.compositions() != null) {
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
    // Carrega estoque atual de matérias-primas em um Map virtual
    Map<Long, Double> currentStock = rawMaterialRepository.listAll()
        .stream()
        .collect(Collectors.toMap(RawMaterial::getCode, RawMaterial::getStockQuantity));

    // Busca produtos ordenados pelo maior valor usando o método do Repository
    List<Product> products = repository.listAllOrderedByPriceDesc();

    List<ProductionSuggestionResponseDTO> suggestions = new ArrayList<>();

    // Para cada produto, calcula a quantidade máxima possível baseada no estoque
    for (Product product : products) {
      List<ProductComposition> compositions = compositionRepository.find("product.code = ?1", product.getCode()).list();

      if (compositions.isEmpty())
        continue;

      // Calcula o limite baseado em cada matéria-prima
      int maxQuantity = compositions.stream().mapToInt(comp -> {
        double needed = comp.getQuantityNeeded();
        if (needed <= 0)
          return 0;
        double available = currentStock.getOrDefault(comp.getRawMaterial().getCode(), 0.0);
        return (int) (available / needed);
      })
          .min()
          .orElse(0);

      if (maxQuantity > 0) {
        // Deduz do estoque virtual para que os próximos produtos considerem o que
        // restou
        for (ProductComposition comp : compositions) {
          Long rmCode = comp.getRawMaterial().getCode();
          double consumed = comp.getQuantityNeeded() * maxQuantity;
          currentStock.put(rmCode, currentStock.get(rmCode) - consumed);
        }

        // Adiciona à lista de sugestões
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