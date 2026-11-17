package dev.zeba.productcatalogservice.service;

import dev.zeba.productcatalogservice.dto.ProductDto;
import dev.zeba.productcatalogservice.model.Product;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IProductService {
    // OLD (Long id, replaced by UUID, 2026-11-17):
    //Product getProductById(Long id);
    Product getProductById(UUID id);

    Product createProduct(ProductDto productDto);

    // OLD (Long id, replaced by UUID, 2026-11-17):
    //boolean deleteProdcutById(Long id);
    boolean deleteProdcutById(UUID id);

    // OLD (Long id, replaced by UUID, 2026-11-17):
    //Product replaceProduct(Long id, ProductDto productDto);
    Product replaceProduct(UUID id, ProductDto productDto);
    // OLD (Long id, replaced by UUID, 2026-11-17):
    //Product patchProduct(Long id, ProductDto productDto);
    Product patchProduct(UUID id, ProductDto productDto);

    List<Product> getAllProducts();

    Page<Product> getProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
