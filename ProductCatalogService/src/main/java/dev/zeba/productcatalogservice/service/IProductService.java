package dev.zeba.productcatalogservice.service;

import dev.zeba.productcatalogservice.dto.ProductDto;
import dev.zeba.productcatalogservice.model.Product;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface IProductService {
    Product getProductById(Long id);

    Product createProduct(ProductDto productDto);

    boolean deleteProdcutById(Long id);

    Product replaceProduct(Long id, ProductDto productDto);
    Product patchProduct(Long id, ProductDto productDto);

    List<Product> getAllProducts();

    Page<Product> getProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
