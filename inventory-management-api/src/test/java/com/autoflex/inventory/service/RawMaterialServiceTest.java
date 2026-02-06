package com.autoflex.inventory.service;

import com.autoflex.inventory.dto.request.RawMaterialRequestDTO;
import com.autoflex.inventory.dto.response.RawMaterialResponseDTO;
import com.autoflex.inventory.mapper.RawMaterialMapper;
import com.autoflex.inventory.model.RawMaterial;
import com.autoflex.inventory.repository.ProductCompositionRepository;
import com.autoflex.inventory.repository.RawMaterialRepository;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RawMaterialServiceTest {

    @InjectMocks
    RawMaterialService service;

    @Mock
    RawMaterialRepository repository;

    @Mock
    RawMaterialMapper mapper;

    @Mock
    ProductCompositionRepository compositionRepository;

    private RawMaterial createEntity(Long code, String name, Double qty) {
        RawMaterial rm = new RawMaterial();
        rm.setCode(code);
        rm.setName(name);
        rm.setStockQuantity(qty);
        return rm;
    }

    @Test
    @DisplayName("Deve retornar lista de materiais quando houver registros")
    void listAll_ShouldReturnList_WhenDataExists() {

        RawMaterial entity = createEntity(1L, "Aço", 100.0);
        RawMaterialResponseDTO responseDTO = new RawMaterialResponseDTO(1L, "Aço", 100.0);

        when(repository.listAll()).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(responseDTO);

        List<RawMaterialResponseDTO> result = service.listAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Aço", result.get(0).name());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver registros")
    void listAll_ShouldReturnEmpty_WhenNoData() {
        when(repository.listAll()).thenReturn(Collections.emptyList());

        List<RawMaterialResponseDTO> result = service.listAll();

        assertTrue(result.isEmpty());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Deve retornar material quando ID existir")
    void getByCode_ShouldReturnMaterial_WhenIdExists() {
        Long id = 1L;
        RawMaterial entity = createEntity(id, "Madeira", 50.0);
        RawMaterialResponseDTO dto = new RawMaterialResponseDTO(id, "Madeira", 50.0);

        when(repository.findById(id)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(dto);

        RawMaterialResponseDTO result = service.getByCode(id);

        assertNotNull(result);
        assertEquals(id, result.code());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando ID não existir")
    void getByCode_ShouldThrowException_WhenIdDoesNotExist() {
        Long id = 99L;
        when(repository.findById(id)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.getByCode(id));
    }

    @Test
    @DisplayName("Deve criar e persistir material com sucesso")
    void create_ShouldPersistAndReturnDTO() {
        RawMaterialRequestDTO request = new RawMaterialRequestDTO("Plástico", 200.0);
        RawMaterial entityToSave = createEntity(null, "Plástico", 200.0);
        RawMaterialResponseDTO response = new RawMaterialResponseDTO(1L, "Plástico", 200.0);

        when(mapper.toEntity(request)).thenReturn(entityToSave);
        doNothing().when(repository).persist(entityToSave);
        when(mapper.toResponse(entityToSave)).thenReturn(response);

        RawMaterialResponseDTO result = service.create(request);

        assertNotNull(result);
        assertEquals(1L, result.code());
        verify(repository, times(1)).persist(entityToSave);
    }

    @Test
    @DisplayName("Deve atualizar nome e estoque quando ID existir")
    void update_ShouldUpdateFields_WhenIdExists() {
        Long id = 1L;
        RawMaterialRequestDTO request = new RawMaterialRequestDTO("Ferro Atualizado", 50.0);
        RawMaterial existingEntity = createEntity(id, "Ferro Antigo", 10.0);

        when(repository.findById(id)).thenReturn(existingEntity);

        RawMaterialResponseDTO expectedResponse = new RawMaterialResponseDTO(id, "Ferro Atualizado", 50.0);
        when(mapper.toResponse(existingEntity)).thenReturn(expectedResponse);

        RawMaterialResponseDTO result = service.update(id, request);

        assertEquals("Ferro Atualizado", result.name());
        assertEquals(50.0, result.stockQuantity());

        assertEquals("Ferro Atualizado", existingEntity.getName());
        assertEquals(50.0, existingEntity.getStockQuantity());
    }

    @Test
    @DisplayName("Deve aceitar estoque zero na atualização")
    void update_ShouldAcceptZeroStock() {
        Long id = 1L;

        RawMaterialRequestDTO request = new RawMaterialRequestDTO("Item Esgotado", 0.0);
        RawMaterial existingEntity = createEntity(id, "Item", 10.0);

        when(repository.findById(id)).thenReturn(existingEntity);
        when(mapper.toResponse(any())).thenReturn(new RawMaterialResponseDTO(id, "Item Esgotado", 0.0));

        service.update(id, request);

        assertEquals(0.0, existingEntity.getStockQuantity());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar atualizar ID inexistente")
    void update_ShouldThrowException_WhenIdDoesNotExist() {
        Long id = 99L;
        RawMaterialRequestDTO request = new RawMaterialRequestDTO("Teste", 10.0);

        when(repository.findById(id)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.update(id, request));
    }

    @Test
    @DisplayName("Deve deletar material quando não estiver em uso")
    void delete_ShouldDelete_WhenNotUsedInComposition() {
        Long id = 1L;
        RawMaterial entity = createEntity(id, "Vidro", 10.0);

        when(repository.findById(id)).thenReturn(entity);
        when(compositionRepository.existsByRawMaterialCode(id)).thenReturn(false);

        service.delete(id);

        verify(repository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar Conflito (409) quando material estiver em uso")
    void delete_ShouldThrowConflict_WhenUsedInComposition() {
        Long id = 1L;
        RawMaterial entity = createEntity(id, "Motor", 5.0);

        when(repository.findById(id)).thenReturn(entity);
        when(compositionRepository.existsByRawMaterialCode(id)).thenReturn(true);

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> service.delete(id));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), exception.getResponse().getStatus());
        verify(repository, never()).deleteById(any());
    }
}