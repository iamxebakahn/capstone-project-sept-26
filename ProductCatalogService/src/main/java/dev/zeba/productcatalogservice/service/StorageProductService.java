package dev.zeba.productcatalogservice.service;

import dev.zeba.productcatalogservice.dto.ProductDto;
import dev.zeba.productcatalogservice.mappers.ProductMapper;
import dev.zeba.productcatalogservice.model.Category;
import dev.zeba.productcatalogservice.model.Product;
import dev.zeba.productcatalogservice.model.enums.State;
import dev.zeba.productcatalogservice.repository.CategoryRepository;
import dev.zeba.productcatalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Primary
@Slf4j
@RequiredArgsConstructor // The more the dependencies the constructor will keep on growing, hence the annotation
//it generates constructor for all the final fields; part of lombok
public class StorageProductService implements  IProductService{
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

/*
    public StorageProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }
*/
    @Override
    public Product getProductById(Long id) {
//        Optional<Product> productOptional = productRepository.findById(id);
//        //findById : returns obj based on P.K. : returns Optional
//        //findAll(): select *
//        //deletebyId, deleteAll()
//        if(productOptional.isEmpty()){
//            return null;
//        }
//
//        return productOptional.get();
        log.info("Reading product from DB:" + id);
        return productRepository.findByIdAndState(id,State.ACTIVE).orElse(null);
    }

    @Override
    public Product createProduct(ProductDto productDto) {
        //dto ->entity
        //validations: value correct or not?
        //check if category exists
        //if not create category
        //if yes get create category
        //save(productDto)

        Product product = productMapper.toEntity(productDto);
        product.setCategory(findOrCreateCategory(product.getCategory()));
        product.setCreatedAt(new Date());
        product.setLastUpdatedAt(new Date());
        product.setState(State.ACTIVE);
        return productRepository.save(product); //due to cascade child entity : category will also be saved
    }

    @Override
    public boolean deleteProdcutById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if(product == null)
            return false;
        product.setState(State.INACTIVE);
        product.setLastUpdatedAt(new Date());
        productRepository.save(product);
        return true;
    }

    @Override
    public Product replaceProduct(Long id, ProductDto productDto) {
        Product existing = productRepository.findByIdAndState(id,State.ACTIVE).orElse(null);
        if(existing == null)
            return null;

        Product incoming = from(productDto);
        existing.setName(incoming.getName());
        existing.setDescription(incoming.getDescription());
        existing.setPrice(incoming.getPrice());
        existing.setImageUrl(incoming.getImageUrl());
        existing.setCategory(findOrCreateCategory(incoming.getCategory()));
        existing.setLastUpdatedAt(new Date());

        return productRepository.save(existing);
    }

    @Override
    public Product patchProduct(Long id, ProductDto productDto) {
        Product existing = productRepository.findByIdAndState(id,State.ACTIVE).orElse(null);
        if(existing == null)
            return null;

        if(productDto.getName()!= null)
            existing.setName(productDto.getName());

        if(productDto.getDescription()!=null)
            existing.setDescription(productDto.getDescription());

        if(productDto.getPrice()!=null)
            existing.setPrice(productDto.getPrice());

        if(productDto.getImageUrl()!=null)
            existing.setImageUrl(productDto.getImageUrl());

        if(productDto.getCategoryDto()!=null){
            Category category = new Category();
            category.setName(productDto.getCategoryDto().getName());
            category.setDescription(productDto.getCategoryDto().getDescription());
            existing.setCategory(findOrCreateCategory(category));
        }

        existing.setLastUpdatedAt(new Date());
        return productRepository.save(existing);
    }

    private Category findOrCreateCategory(Category category){
        if(category == null){
            return null;
        }

        Optional<Category> existing = categoryRepository.findByName(category.getName()); //Custom JPA method: findBy[Attribute Name] ; findByGenderAndAgeGreaterThan
        if(existing.isPresent()) {
            return existing.get();
        }

        Date now = new Date();
        category.setId(null);
        category.setCreatedAt(now);
        category.setLastUpdatedAt(now);
        return category;
    }
    private Product from(ProductDto dto){
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());

        if(dto.getCategoryDto()!=null){
            Category category = new Category();
            category.setName(dto.getCategoryDto().getName());
            category.setDescription(dto.getCategoryDto().getDescription());
            product.setCategory(category);
        }

        return product;
    }

    public List<Product> getAllProducts(){
        return productRepository.findAllProducts();
    }

    @Override
    public Page<Product> getProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceBetweenAndState(minPrice,maxPrice,State.ACTIVE,pageable);
    }

    //PUT /UPDATE
    //2 ways to update APIs: PUT/PATCH: PUT replaces entire object/ row of db: PATCH: partial update to existing object
    //I/p of PUT: DTO  and id; PATCH: i/p is DTO, fields are nullable : check fields whichever is not null, is replaced
    // bcoz PUT updates entire object, if only 1 field is sent in i/p DTO rest other fields are set to null, thats why PATCH is used, in such cases coz entire object is not worked upon,
    // only non null fields are updated and returned
    //standard is given for PUT and PATCH, implementation lies with programmer, SPRING cant enforce this behaviour
    //delete: JPA method deelete does hard delete and removed entire row, we dont use that, we soft delete, set state to inactive only
}
