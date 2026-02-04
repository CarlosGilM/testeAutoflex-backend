package com.autoflex.inventory.controller;

import com.autoflex.inventory.dto.request.RawMaterialRequestDTO;
import com.autoflex.inventory.dto.response.RawMaterialResponseDTO;
import com.autoflex.inventory.service.RawMaterialService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@Path("/api/raw-materials")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Matérias-primas", description = "Gerenciamento do estoque de insumos")
public class RawMaterialResource {

    @Inject
    RawMaterialService service;

    @GET
    @Operation(summary = "Listar matérias-primas", description = "Retorna todos os insumos e suas quantidades em estoque")
    @APIResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    public List<RawMaterialResponseDTO> listAll() {
        return service.listAll();
    }

    @GET
    @Path("/{code}")
    @Operation(summary = "Obter matéria-prima por código", description = "Busca detalhes de um insumo específico")
    @APIResponse(responseCode = "200", description = "Insumo encontrado")
    public RawMaterialResponseDTO getByCode(@PathParam("code") Long code) {
        return service.getByCode(code);
    }

    @POST
    @Operation(summary = "Cadastrar nova matéria-prima", description = "Adiciona um novo insumo ao estoque")
    @APIResponse(responseCode = "201", description = "Insumo cadastrado com sucesso")
    public Response create(RawMaterialRequestDTO dto) {
        RawMaterialResponseDTO responseDTO = service.create(dto);
        return Response.created(URI.create("/api/raw-materials/" + responseDTO.code()))
                .entity(responseDTO)
                .build();
    }

    @PUT
    @Path("/{code}")
    @Operation(summary = "Atualizar estoque/dados", description = "Atualiza nome ou quantidade de uma matéria-prima")
    public RawMaterialResponseDTO update(@PathParam("code") Long code, RawMaterialRequestDTO dto) {
        return service.update(code, dto);
    }

    @DELETE
    @Path("/{code}")
    @Operation(summary = "Remover matéria-prima", description = "Remove o insumo se não houver vínculos (RF002)")
    @APIResponse(responseCode = "204", description = "Removido com sucesso")
    @APIResponse(responseCode = "409", description = "Conflito: insumo em uso")
    public Response delete(@PathParam("code") Long code) {
        service.delete(code);
        return Response.noContent().build();
    }
}