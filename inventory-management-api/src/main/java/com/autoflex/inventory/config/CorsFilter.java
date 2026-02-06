package com.autoflex.inventory.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

        @Override
        public void filter(ContainerRequestContext requestContext,
                        ContainerResponseContext responseContext) throws IOException {

                // Permite EXATAMENTE o seu frontend
                responseContext.getHeaders().add(
                                "Access-Control-Allow-Origin", "http://localhost:5173");

                // Permite credenciais (cookies, headers de auth)
                responseContext.getHeaders().add(
                                "Access-Control-Allow-Credentials", "true");

                // Permite os cabeçalhos que o navegador pede
                responseContext.getHeaders().add(
                                "Access-Control-Allow-Headers",
                                "origin, content-type, accept, authorization, x-requested-with");

                // Permite os métodos HTTP
                responseContext.getHeaders().add(
                                "Access-Control-Allow-Methods",
                                "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        }
}