package dev.zeba.productcatalogservice.service;

import dev.zeba.productcatalogservice.dto.DummyJsonProductDto;
import dev.zeba.productcatalogservice.dto.DummyJsonProductListDto;
import dev.zeba.productcatalogservice.dto.ProductDto;
import dev.zeba.productcatalogservice.model.Category;
import dev.zeba.productcatalogservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DummyProductService implements IProductService {
    //For 3rd party API calls via spring: Rest Template, Rest Client, Feign Client (we use Rest Client)
    //Study Feign Client

    private final RestClient restClient;

    public DummyProductService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    // OLD (Long id, replaced by UUID, 2026-11-17):
    //public Product getProductById(Long id) {
    public Product getProductById(UUID id) {
        try {
            ResponseEntity<DummyJsonProductDto> dummyJsonProductDto = restClient.get()
                    // TODO(UUID): supplier id is Long
                    .uri("https://dummyjson.com/products/{id}", id)
                    .retrieve()
                    .toEntity(DummyJsonProductDto.class);
            if(dummyJsonProductDto.getStatusCode().is2xxSuccessful() && dummyJsonProductDto.hasBody())
                return from(dummyJsonProductDto.getBody());

            return  null;
        }
        catch (HttpClientErrorException.NotFound e){
            return  null;
        }
    }

    @Override
    public Product createProduct(ProductDto productDto) {
        return null;
    }

    @Override
    // OLD (Long id, replaced by UUID, 2026-11-17):
    //public boolean deleteProdcutById(Long id) {
    public boolean deleteProdcutById(UUID id) {
        return false;
    }

    @Override
    // OLD (Long id, replaced by UUID, 2026-11-17):
    //public Product replaceProduct(Long id, ProductDto productDto) {
    public Product replaceProduct(UUID id, ProductDto productDto) {
        return null;
    }

    @Override
    // OLD (Long id, replaced by UUID, 2026-11-17):
    //public Product patchProduct(Long id, ProductDto productDto) {
    public Product patchProduct(UUID id, ProductDto productDto) {
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        ResponseEntity<DummyJsonProductListDto> response = restClient.get()
                .uri("https://dummyjson.com/products?limit=0")
                .retrieve()
                .toEntity(DummyJsonProductListDto.class);
        List<Product> products = new ArrayList<>();
        for(DummyJsonProductDto dummyJsonProductDto: response.getBody().getProducts()){
            products.add(from(dummyJsonProductDto));
        }
        return products;
    }

    @Override
    public Page<Product> getProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return null;
    }


    private Product from(DummyJsonProductDto dummyJsonProductDto){
        Product product = new Product();
        // TODO(UUID): supplier id is Long
        //product.setId(dummyJsonProductDto.getId());
        product.setName(dummyJsonProductDto.getTitle());
        product.setDescription(dummyJsonProductDto.getDescription());
        product.setPrice(dummyJsonProductDto.getPrice());
        product.setImageUrl(dummyJsonProductDto.getThumbnail());
        Category category = new Category();
        category.setName(dummyJsonProductDto.getCategory());
        product.setCategory(category);
        return product;
    }

}
