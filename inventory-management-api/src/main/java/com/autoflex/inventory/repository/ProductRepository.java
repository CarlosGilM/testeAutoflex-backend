package com.autoflex.inventory.repository;

import com.autoflex.inventory.model.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    public List<Product> listAllOrderedByPriceDesc() {
        return list("ORDER BY price DESC");
    }
}