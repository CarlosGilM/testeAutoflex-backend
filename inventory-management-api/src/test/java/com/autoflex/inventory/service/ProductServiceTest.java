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
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    ProductService service;

    @Mock
    ProductRepository repository;

    @Mock
    ProductMapper mapper;

    @Mock
    RawMaterialRepository rawMaterialRepository;

    @Mock
    ProductCompositionRepository compositionRepository;

    private Product createProduct(Long code, String name, double price) {
        Product p = new Product();
        p.setCode(code);
        p.setName(name);
        p.setPrice(BigDecimal.valueOf(price));
        return p;
    }

    private RawMaterial createRawMaterial(Long code, String name, Double stock) {
        RawMaterial rm = new RawMaterial();
        rm.setCode(code);
        rm.setName(name);
        rm.setStockQuantity(stock);
        return rm;
    }

    private ProductComposition createComposition(Product p, RawMaterial rm, double qtyNeeded) {
        ProductComposition pc = new ProductComposition();
        pc.setProduct(p);
        pc.setRawMaterial(rm);
        pc.setQuantityNeeded(qtyNeeded);
        return pc;
    }

    @Test
    @DisplayName("Deve retornar lista de produtos com suas composições")
    void listAll_ShouldReturnList() {
        Product product = createProduct(1L, "Mesa", 150.0);
        ProductResponseDTO responseDTO = new ProductResponseDTO(1L, "Mesa", BigDecimal.valueOf(150.0),
                Collections.emptyList());

        when(repository.listAll()).thenReturn(List.of(product));
        when(compositionRepository.list("product.code", 1L)).thenReturn(new ArrayList<>());
        when(mapper.toResponse(eq(product), anyList())).thenReturn(responseDTO);

        List<ProductResponseDTO> result = service.listAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(compositionRepository, times(1)).list("product.code", 1L);
    }

    @Test
    @DisplayName("Deve retornar produto quando ID existir")
    void getByCode_ShouldReturnProduct_WhenExists() {
        Long id = 1L;
        Product product = createProduct(id, "Cadeira", 50.0);
        ProductResponseDTO responseDTO = new ProductResponseDTO(id, "Cadeira", BigDecimal.valueOf(50.0),
                Collections.emptyList());

        when(repository.findById(id)).thenReturn(product);
        when(compositionRepository.list("product.code", id)).thenReturn(new ArrayList<>());
        when(mapper.toResponse(eq(product), anyList())).thenReturn(responseDTO);

        ProductResponseDTO result = service.getByCode(id);

        assertNotNull(result);
        assertEquals(id, result.code());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando produto não existir")
    void getByCode_ShouldThrowException_WhenIdDoesNotExist() {
        when(repository.findById(99L)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> service.getByCode(99L));
    }

    @Test
    @DisplayName("Deve criar produto com composição válida")
    void create_ShouldPersistProductAndCompositions() {
        ProductCompositionRequestDTO compReq = new ProductCompositionRequestDTO(null, 10L, 2.0);
        ProductRequestDTO request = new ProductRequestDTO("Mesa", BigDecimal.valueOf(200), List.of(compReq));

        Product productEntity = createProduct(null, "Mesa", 200);
        RawMaterial rawMaterial = createRawMaterial(10L, "Madeira", 100.0);

        when(mapper.toEntity(request)).thenReturn(productEntity);
        doNothing().when(repository).persist(productEntity);

        when(rawMaterialRepository.findById(10L)).thenReturn(rawMaterial);

        doNothing().when(compositionRepository).persist(any(ProductComposition.class));

        when(compositionRepository.list("product.code", productEntity.getCode())).thenReturn(new ArrayList<>());
        when(mapper.toResponse(any(), anyList()))
                .thenReturn(new ProductResponseDTO(1L, "Mesa", BigDecimal.valueOf(200), Collections.emptyList()));

        ProductResponseDTO result = service.create(request);

        assertNotNull(result);
        verify(repository).persist(productEntity);
        verify(compositionRepository).persist(any(ProductComposition.class));
    }

    @Test
    @DisplayName("Deve lançar BadRequestException se a receita for vazia")
    void create_ShouldThrowException_WhenCompositionIsEmpty() {
        ProductRequestDTO request = new ProductRequestDTO("Mesa", BigDecimal.valueOf(200), Collections.emptyList());

        assertThrows(BadRequestException.class, () -> service.create(request));
        verify(repository, never()).persist(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar NotFoundException se a matéria-prima não existir")
    void create_ShouldThrowException_WhenRawMaterialNotFound() {
        ProductCompositionRequestDTO compReq = new ProductCompositionRequestDTO(null, 99L, 1.0);
        ProductRequestDTO request = new ProductRequestDTO("Falha", BigDecimal.valueOf(10), List.of(compReq));
        Product productEntity = createProduct(1L, "Falha", 10);

        when(mapper.toEntity(request)).thenReturn(productEntity);
        doNothing().when(repository).persist(productEntity);
        when(rawMaterialRepository.findById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.create(request));
    }

    @Test
    @DisplayName("Deve atualizar apenas nome e preço, ignorando receita")
    void update_ShouldUpdateNameAndPrice() {
        Long id = 1L;
        ProductRequestDTO request = new ProductRequestDTO("Mesa Nova", BigDecimal.valueOf(300), null);
        Product existingProduct = createProduct(id, "Mesa Velha", 100);

        when(repository.findById(id)).thenReturn(existingProduct);
        when(compositionRepository.list("product.code", id)).thenReturn(new ArrayList<>());
        when(mapper.toResponse(eq(existingProduct), anyList()))
                .thenReturn(new ProductResponseDTO(id, "Mesa Nova", BigDecimal.valueOf(300), Collections.emptyList()));

        service.update(id, request);

        assertEquals("Mesa Nova", existingProduct.getName());
        assertEquals(BigDecimal.valueOf(300), existingProduct.getPrice());

        verify(compositionRepository, never()).persist(any(ProductComposition.class));
        verify(compositionRepository, never()).delete(anyString(), anyLong());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar atualizar produto inexistente")
    void update_ShouldThrowException_WhenProductNotFound() {
        when(repository.findById(99L)).thenReturn(null);
        assertThrows(NotFoundException.class,
                () -> service.update(99L, new ProductRequestDTO("X", BigDecimal.ONE, null)));
    }

    @Test
    @DisplayName("Deve deletar composições e depois o produto")
    void delete_ShouldDeleteCompositionsThenProduct() {
        Long id = 1L;
        Product product = createProduct(id, "Mesa", 100);
        when(repository.findById(id)).thenReturn(product);

        service.delete(id);

        verify(compositionRepository).delete("product.code", id);
        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar deletar produto inexistente")
    void delete_ShouldThrowException_WhenProductNotFound() {
        when(repository.findById(99L)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> service.delete(99L));
        verify(compositionRepository, never()).delete(anyString(), anyLong());
    }

    @Test
    @DisplayName("Deve calcular produção corretamente priorizando produtos mais caros")
    void getProductionSuggestion_ShouldPrioritizeExpensiveProducts() {
        RawMaterial madeira = createRawMaterial(100L, "Madeira", 10.0);
        when(rawMaterialRepository.listAll()).thenReturn(List.of(madeira));

        Product prodA = createProduct(1L, "Mesa Luxo", 200.0);
        Product prodB = createProduct(2L, "Banco Simples", 50.0);
        when(repository.listAllOrderedByPriceDesc()).thenReturn(List.of(prodA, prodB));

        ProductComposition compA = createComposition(prodA, madeira, 2.0);
        ProductComposition compB = createComposition(prodB, madeira, 1.0);

        when(compositionRepository.listAll()).thenReturn(List.of(compA, compB));

        List<ProductionSuggestionResponseDTO> result = service.getProductionSuggestion();

        assertEquals(1, result.size());

        ProductionSuggestionResponseDTO suggestionA = result.get(0);
        assertEquals("Mesa Luxo", suggestionA.productName());
        assertEquals(5, suggestionA.quantityToProduce());
        assertEquals(BigDecimal.valueOf(1000.0), suggestionA.totalEstimatedValue());
    }

    @Test
    @DisplayName("Não deve sugerir produção se não houver estoque suficiente")
    void getProductionSuggestion_ShouldReturnEmpty_WhenNoStock() {
        RawMaterial ouro = createRawMaterial(10L, "Ouro", 0.5);
        when(rawMaterialRepository.listAll()).thenReturn(List.of(ouro));

        Product anel = createProduct(1L, "Anel", 500.0);
        when(repository.listAllOrderedByPriceDesc()).thenReturn(List.of(anel));

        ProductComposition comp = createComposition(anel, ouro, 1.0);
        when(compositionRepository.listAll()).thenReturn(List.of(comp));

        List<ProductionSuggestionResponseDTO> result = service.getProductionSuggestion();

        assertTrue(result.isEmpty());
    }
}