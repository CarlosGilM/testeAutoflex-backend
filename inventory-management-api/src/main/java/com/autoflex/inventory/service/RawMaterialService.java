package com.autoflex.inventory.service;

import com.autoflex.inventory.dto.request.RawMaterialRequestDTO;
import com.autoflex.inventory.dto.response.RawMaterialResponseDTO;
import com.autoflex.inventory.mapper.RawMaterialMapper;
import com.autoflex.inventory.model.RawMaterial;
import com.autoflex.inventory.repository.ProductCompositionRepository;
import com.autoflex.inventory.repository.RawMaterialRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RawMaterialService {

    @Inject
    RawMaterialRepository repository;
    @Inject
    RawMaterialMapper mapper;
    @Inject
    ProductCompositionRepository compositionRepository;

    public List<RawMaterialResponseDTO> listAll() {
        return repository.listAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public RawMaterialResponseDTO getByCode(Long code) {
        RawMaterial entity = repository.findById(code);
        if (entity == null)
            throw new NotFoundException("Raw Material not found");
        return mapper.toResponse(entity);
    }

    @Transactional
    public RawMaterialResponseDTO create(RawMaterialRequestDTO dto) {
        RawMaterial entity = mapper.toEntity(dto);
        repository.persist(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public RawMaterialResponseDTO update(Long code, RawMaterialRequestDTO dto) {
        RawMaterial entity = repository.findById(code);
        if (entity == null)
            throw new NotFoundException("Raw Material not found");

        entity.setName(dto.name());
        entity.setStockQuantity(dto.stockQuantity());
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long code) {
        RawMaterial entity = repository.findById(code);
        if (entity == null)
            throw new NotFoundException("Raw Material not found");

        boolean isUsed = compositionRepository.existsByRawMaterialCode(code);
        if (isUsed) {
            throw new WebApplicationException(
                    "Não é possível excluir: esta matéria-prima faz parte da composição de um produto.",
                    Response.Status.CONFLICT);
        }

        repository.deleteById(code);
    }
}