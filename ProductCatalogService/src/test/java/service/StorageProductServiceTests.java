package service;

import dev.zeba.productcatalogservice.dto.ProductDto;
import dev.zeba.productcatalogservice.mappers.ProductMapper;
import dev.zeba.productcatalogservice.model.Category;
import dev.zeba.productcatalogservice.model.Product;
import dev.zeba.productcatalogservice.repository.CategoryRepository;
import dev.zeba.productcatalogservice.repository.ProductRepository;
import dev.zeba.productcatalogservice.service.StorageProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//repositories are mocked, so no database is needed: the PATCH flow never leaves this class
@ExtendWith(MockitoExtension.class)//to write mocks need to enable this
public class StorageProductServiceTests {
    @Mock//tells JUnit frameork to not autowire original framework, create empty obj w/o any cod
    private ProductRepository productRepository; //since its mocked, doesnt have save func written in it
    @Mock
    private ProductMapper productMapper;
    @Mock
    private CategoryRepository categoryRepository;
    //class being tested has to have real obj

    @InjectMocks //insert mock objs into real obj
    private StorageProductService service;

    @Test
    void createProduct_clientSendsAnId_savesWithoutItAndWithDates() {
        ProductDto input = new ProductDto();
        input.setId(UUID.randomUUID());
        input.setName("Mascara");

        Product product = new Product();
        // product.setId(dto.getId());
        product.setName("Mascara");
        product.setCategory(new Category());
        //mock mapper.toEntity and repo.save

        when(productRepository.save(any(Product.class)))
                .thenAnswer(call -> call.getArgument(0)); //returns dummy value after mocking :stubs

        when(productMapper.toEntity(any(ProductDto.class)))
                .thenReturn(product); //returns dummy value after mocking :stubs

        Product result = service.createProduct(input);

        assertThat(result.getName()).isEqualTo("Mascara");

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class); //argument capture: tracks obj even after func call
        verify(productRepository).save(captor.capture());  // verifying if call has been made or not
        assertThat(captor.getValue().getId()).isNull();
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getLastUpdatedAt()).isNotNull();

        // Unit tests : write test cases without getting influenced by call
        // integration tests - E2E don't mock the externals calls

        // search product - catch minPrice maxPrice bug
    }
}
