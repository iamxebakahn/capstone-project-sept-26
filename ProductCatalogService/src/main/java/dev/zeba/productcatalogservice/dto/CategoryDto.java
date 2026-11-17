package dev.zeba.productcatalogservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryDto {
    // OLD (Long id, replaced by UUID, 2026-11-17):
    //private Long id;
    private UUID id;
    private String name;
    private String description;
}
