package dev.zeba.productcatalogservice.dto;


import dev.zeba.productcatalogservice.model.Category;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    //private Category category; // Cant have another entity being exposed here, hence go for a separate DTO for the entity being exposed
    private CategoryDto categoryDto;
    private String imageUrl;

}
