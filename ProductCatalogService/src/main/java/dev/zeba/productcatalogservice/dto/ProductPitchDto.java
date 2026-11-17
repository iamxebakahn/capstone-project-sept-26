package dev.zeba.productcatalogservice.dto;

import dev.zeba.productcatalogservice.model.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductPitchDto {
    // OLD (Long id, replaced by UUID, 2026-11-17):
    //private Long id;
    private UUID id;
    private String pitch;


}
