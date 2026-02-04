package com.autoflex.inventory.controller;

import com.autoflex.inventory.dto.request.ProductRequestDTO;
import com.autoflex.inventory.dto.response.ProductResponseDTO;
import com.autoflex.inventory.dto.response.ProductionSuggestionResponseDTO;
import com.autoflex.inventory.service.ProductService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Produtos", description = "Gerenciamento do cadastro de produtos e suas composições")
public class ProductResource {

  @Inject
  ProductService service;

  @GET
  @Operation(summary = "Listar todos os produtos", description = "Retorna uma lista de todos os produtos cadastrados")
  public List<ProductResponseDTO> listAll() {
    return service.listAll();
  }

  @GET
  @Path("/{code}")
  @Operation(summary = "Obter produto por código", description = "Busca os detalhes de um produto específico")
  @APIResponse(responseCode = "200", description = "Produto encontrado")
  @APIResponse(responseCode = "404", description = "Produto não encontrado")
  public ProductResponseDTO getByCode(@PathParam("code") Long code) {
    return service.getByCode(code);
  }

  @POST
  @Operation(summary = "Criar novo produto", description = "Cadastra um novo produto no sistema")
  @APIResponse(responseCode = "201", description = "Produto criado com sucesso")
  public Response create(ProductRequestDTO dto) {
    ProductResponseDTO responseDTO = service.create(dto);
    return Response.created(URI.create("/api/products/" + responseDTO.code()))
        .entity(responseDTO)
        .build();
  }

  @PUT
  @Path("/{code}")
  @Operation(summary = "Atualizar produto", description = "Atualiza os dados básicos de um produto existente")
  public ProductResponseDTO update(@PathParam("code") Long code, ProductRequestDTO dto) {
    return service.update(code, dto);
  }

  @DELETE
  @Path("/{code}")
  @Operation(summary = "Remover produto", description = "Exclui um produto e seus vínculos")
  @APIResponse(responseCode = "204", description = "Produto removido com sucesso")
  public Response delete(@PathParam("code") Long code) {
    service.delete(code);
    return Response.noContent().build();
  }

  @GET
  @Path("/production-suggestion")
  @Operation(summary = "Sugerir produção por estoque", description = "Calcula a quantidade de produtos produzíveis priorizando os de maior valor com base no estoque atual (RF004/RF008)")
  @APIResponse(responseCode = "200", description = "Sugestão de produção gerada com sucesso")
  public List<ProductionSuggestionResponseDTO> getProductionSuggestion() {
    return service.getProductionSuggestion();
  }
}