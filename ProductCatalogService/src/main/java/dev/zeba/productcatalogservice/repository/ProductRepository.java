package dev.zeba.productcatalogservice.repository;

import dev.zeba.productcatalogservice.model.Product;
import dev.zeba.productcatalogservice.model.enums.State;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
//why interface whynot a class: coz implementation needs to be done for all methods in JPA instead hibernate has already implemented these methods,
// so we use thede methods instead of implementing them
public interface ProductRepository extends JpaRepository<Product,Long> {

    Optional<Product> findByIdAndState(Long id, State state);

    //2 methods of using JPA a. inbuilt: findAll(), findById() b. custom JPA function: findByName() : filter /reading/sorting/searching no computations
    //or calculations can be done like. avg, sum etc
    //final resort we write query on our own: @Query -> 2 syntaxes : JPQL and SQL(Native query or normal way of writing SQL)
    //prod preference JPQL (written in terms of Java entities)

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByPriceBetweenAndState(BigDecimal minPrice, BigDecimal maxPrice, State state, Pageable pageable);

    //Fetch is to overcome N+1 problem in JPQL
    @Query("SELECT p from Product p LEFT JOIN FETCH p.category c")
    List<Product> findAllProducts();
    //to check whether our code works as expected
    //APIs on postman : manual testing
    //code checked at compilation time: to see if nobody has changed business logic and stuff eg. sum() is 5 and should
    // be 5 and doesnot change business logic, and can only at max optimize!
    //like 5 shouldn't change to 6: test cases are solution for it, business logic shouldn't change, only optimization
    //and bug fixes
    //Types of Tests (at code level):1. Unit Tests (testing small parts in isolation (separated from other funcs). like sortandsave in service layer sorts and saves data in db
    //if sort worked n some issue's in save due to repo issue, func will sort but not save, hence separate tests for both functionalities
    // 2. Integration Tests : tests interaction b/w components : service is able to call repo, controller is able to call service or not
    // 3. E2E Tests: tests entire user journey: creates prod: tests if i get prod correctly or not, user is able to signup correctly or not
    // user is able to delete profile or not: for every user journey write end to end tests
    // unit test: most volume, max > integration > E2E test
    // compile code: fastest to slowest : unit> integration > E2E
    // Junit - library used to write test cases; lastest version Junit5 : not spring but Java lib
    //lombok: java library
    //
}

