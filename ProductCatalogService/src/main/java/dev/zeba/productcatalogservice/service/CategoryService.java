package dev.zeba.productcatalogservice.service;

import dev.zeba.productcatalogservice.dto.CategorySummaryDto;
import dev.zeba.productcatalogservice.model.enums.State;
import dev.zeba.productcatalogservice.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService implements ICategoryService{
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Double getAvgPriceForCategory(String category) {
        return categoryRepository.findAvgPriceForCategory(category);
    }

    @Override
    public List<CategorySummaryDto> getCategorySummaries() {
        return categoryRepository.findCategorySummaries(State.ACTIVE);
    }
}

//PAgination: Breaking data into chunks: Page size: entries in 1 chunk: 1 page = 1 chunk
//frontend can give chunk number as well as number of entries in 1 chunk
//searching/filtering v.imp in ecoms