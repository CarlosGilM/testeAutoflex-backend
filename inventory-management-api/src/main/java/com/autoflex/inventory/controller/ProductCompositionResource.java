package com.autoflex.inventory.controller;

import com.autoflex.inventory.dto.request.ProductCompositionRequestDTO;
import com.autoflex.inventory.dto.response.ProductCompositionResponseDTO;
import com.autoflex.inventory.service.ProductCompositionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/compositions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Composição de Produtos", description = "Gerenciamento da associação entre produtos e matérias-primas (Receitas)")
public class ProductCompositionResource {

    @Inject
    ProductCompositionService service;

    @GET
    @Path("/product/{productCode}")
    @Operation(summary = "Listar composição por produto", description = "Retorna todos os insumos vinculados a um produto")
    @APIResponse(responseCode = "200", description = "Composição recuperada com sucesso")
    public List<ProductCompositionResponseDTO> listByProduct(@PathParam("productCode") Long productCode) {
        return service.listByProduct(productCode);
    }

    @POST
    @Operation(summary = "Adicionar insumo ao produto", description = "Vincula uma matéria-prima a um produto (RF003)")
    @APIResponse(responseCode = "201", description = "Insumo vinculado com sucesso")
    public Response addIngredient(ProductCompositionRequestDTO dto) {
        ProductCompositionResponseDTO responseDTO = service.addIngredient(dto);
        return Response.status(Response.Status.CREATED).entity(responseDTO).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualizar quantidade necessária", description = "Altera a quantidade de um insumo em uma composição")
    public ProductCompositionResponseDTO updateQuantity(@PathParam("id") Long id, ProductCompositionRequestDTO dto) {
        return service.updateQuantity(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remover insumo da composição", description = "Desvincula um insumo de um produto")
    @APIResponse(responseCode = "204", description = "Insumo removido com sucesso")
    public Response removeIngredient(@PathParam("id") Long id) {
        service.removeIngredient(id);
        return Response.noContent().build();
    }
}