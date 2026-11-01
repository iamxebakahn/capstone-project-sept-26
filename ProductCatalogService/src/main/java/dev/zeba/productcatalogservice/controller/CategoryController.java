package dev.zeba.productcatalogservice.controller;

import dev.zeba.productcatalogservice.service.ICategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{category}")
    public ResponseEntity<Double> getAvgPriceForCategory(@PathVariable String category){
        Double avg = categoryService.getAvgPriceForCategory(category);
        return new ResponseEntity<>(avg, HttpStatus.OK);
    }
}
