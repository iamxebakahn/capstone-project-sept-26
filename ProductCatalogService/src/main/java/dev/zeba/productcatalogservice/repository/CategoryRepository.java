package dev.zeba.productcatalogservice.repository;

import dev.zeba.productcatalogservice.dto.CategorySummaryDto;
import dev.zeba.productcatalogservice.model.Category;
import dev.zeba.productcatalogservice.model.enums.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    Optional<Category> findByName(String name);

    //if developer is writing his own query, name of function doesnot matter

    //JPQL
    @Query(value = "SELECT AVG(p.price) from Product p where p.category.name=:categoryName") //: to consume i/p values, positional arguments, variable is picked and replaced at runtime
    Double findAvgPriceForCategory(String categoryName);

    @Query("SELECT new dev.zeba.productcatalogservice.dto.CategorySummaryDto(c.name, COUNT(p)) " +
            "FROM Category c LEFT JOIN c.products p ON p.state = :state " +
            "GROUP BY c.name ORDER BY c.name")
    List<CategorySummaryDto> findCategorySummaries(@Param("state") State state);

    //SQL
//    @Query(value = "SELECT avg(p.price) from Product p JOIN category c ON p.category_id = c.id where c.name=:categoryName",nativeQuery = true)
//    Double findAvgPriceForCategory(String categoryName);
}
