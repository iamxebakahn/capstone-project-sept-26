package dev.zeba.productcatalogservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DummyJsonProductDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private BigDecimal price;
    private String thumbnail;
}
