package dev.zeba.productcatalogservice.service;

import dev.zeba.productcatalogservice.dto.DummyJsonProductDto;
import dev.zeba.productcatalogservice.dto.DummyJsonProductListDto;
import dev.zeba.productcatalogservice.dto.ProductDto;
import dev.zeba.productcatalogservice.model.Category;
import dev.zeba.productcatalogservice.model.Product;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DummyProductService implements IProductService {
    //For 3rd party API calls via spring: Rest Template, Rest Client, Feign Client (we use Rest Template)
    //Study Feign Client

    private RestTemplate restTemplate;

    //Java expects RestTemplate object to be created with the builder of RestTemplate
    //builder is not present here though: so we add rest client
    //To get resttamplatebuilder to create resttamplate object, maven repository has dependency code for starter restclient
    //added dependency code in .pom (coz m using SB4 and it has separate dependency for restclient)
    public DummyProductService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Product getProductById(Long id) {
        try {
            ResponseEntity<DummyJsonProductDto> dummyJsonProductDto = restTemplate.getForEntity("https://dummyjson.com/products/{id}", DummyJsonProductDto.class, id);
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
    public boolean deleteProdcutById(Long id) {
        return false;
    }

    @Override
    public Product replaceProduct(Long id, ProductDto productDto) {
        return null;
    }

    @Override
    public Product patchProduct(Long id, ProductDto productDto) {
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        ResponseEntity<DummyJsonProductListDto> response = restTemplate.getForEntity("https://dummyjson.com/products?limit=0", DummyJsonProductListDto.class);
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
        product.setId(dummyJsonProductDto.getId());
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
