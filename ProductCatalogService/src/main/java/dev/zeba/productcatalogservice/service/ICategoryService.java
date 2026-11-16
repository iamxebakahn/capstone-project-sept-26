package dev.zeba.productcatalogservice.service;

import dev.zeba.productcatalogservice.dto.CategorySummaryDto;

import java.util.List;

public interface ICategoryService {
    Double getAvgPriceForCategory(String category);
    List<CategorySummaryDto> getCategorySummaries();
}
