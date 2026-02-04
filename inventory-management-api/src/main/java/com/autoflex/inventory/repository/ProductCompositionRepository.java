package com.autoflex.inventory.repository;

import com.autoflex.inventory.model.ProductComposition;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ProductCompositionRepository implements PanacheRepository<ProductComposition> {

    public List<ProductComposition> findByProductId(Long productCode) {
        return list("product.code", productCode);
    }

    public boolean existsByRawMaterialCode(Long rawMaterialCode) {
        return count("rawMaterial.code", rawMaterialCode) > 0;
    }
}