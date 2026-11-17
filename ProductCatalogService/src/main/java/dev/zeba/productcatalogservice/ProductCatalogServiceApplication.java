package dev.zeba.productcatalogservice;
import org.springframework.resilience.annotation.EnableResilientMethods;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableResilientMethods //for tackling freaky friend: enables retries on methods
public class ProductCatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductCatalogServiceApplication.class, args);
    }

}
