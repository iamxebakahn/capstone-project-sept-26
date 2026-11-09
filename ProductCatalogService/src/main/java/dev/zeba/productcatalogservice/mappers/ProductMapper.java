package dev.zeba.productcatalogservice.mappers;

import dev.zeba.productcatalogservice.dto.CategoryDto;
import dev.zeba.productcatalogservice.dto.ProductDto;
import dev.zeba.productcatalogservice.model.Category;
import dev.zeba.productcatalogservice.model.Product;
import dev.zeba.productcatalogservice.model.enums.State;
import org.springframework.stereotype.Component;

@Component// creates a bean/object of this class to be used to inject into other class
//doing this to reuse logic and testing efficiency improves
public class ProductMapper {
    public ProductDto toDto(Product product){
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setImageUrl(product.getImageUrl());
        if(product.getCategory()!=null){
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName(product.getCategory().getName());
            categoryDto.setDescription(product.getCategory().getDescription());
            productDto.setCategoryDto(categoryDto);
        }
        return productDto;
    }

    public Product applyPatch(Product existing, ProductDto patch){
        if(existing == null || patch == null){
            return existing;
        }

        if(patch.getName() != null)
            existing.setName(patch.getName());

        if(patch.getDescription() != null)
            existing.setDescription(patch.getDescription());

        if(patch.getPrice() != null)
            existing.setPrice(patch.getPrice());

        if(patch.getImageUrl() != null)
            existing.setImageUrl(patch.getImageUrl());

        return existing;
    }

    public Product toEntity(ProductDto productDto){
        Product product = new Product();
        // product.setId(productDto.getId());
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setImageUrl(productDto.getImageUrl());

        if (productDto.getCategoryDto() != null) {
            Category category = new Category();
            category.setName(productDto.getCategoryDto().getName());
            category.setDescription(productDto.getCategoryDto().getDescription());
            category.setState(State.ACTIVE);
            product.setCategory(category);
        }
        return product;
    }
}
