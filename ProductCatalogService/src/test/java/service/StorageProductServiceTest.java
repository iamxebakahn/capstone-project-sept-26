package service;

import dev.zeba.productcatalogservice.dto.CategoryDto;
import dev.zeba.productcatalogservice.dto.ProductDto;
import dev.zeba.productcatalogservice.mappers.ProductMapper;
import dev.zeba.productcatalogservice.model.Category;
import dev.zeba.productcatalogservice.model.Product;
import dev.zeba.productcatalogservice.model.enums.State;
import dev.zeba.productcatalogservice.repository.CategoryRepository;
import dev.zeba.productcatalogservice.repository.ProductRepository;
import dev.zeba.productcatalogservice.service.StorageProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//repositories are mocked, so no database is needed: the PATCH flow never leaves this class
@ExtendWith(MockitoExtension.class)
public class StorageProductServiceTest {

    private static final UUID ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ID_7 = UUID.fromString("00000000-0000-0000-0000-000000000007");
    private static final UUID ID_9 = UUID.fromString("00000000-0000-0000-0000-000000000009");

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private StorageProductService storageProductService;

    @BeforeEach
    public void setup() {
        storageProductService = new StorageProductService(productRepository, categoryRepository, new ProductMapper());
    }

    private Product existingProduct() {
        Product existing = new Product();
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //existing.setId(1L);
        existing.setId(ID_1);
        existing.setName("Mascara");
        existing.setDescription("Volumising mascara");
        existing.setPrice(new BigDecimal("9.99"));
        existing.setImageUrl("https://cdn.shopnest/mascara.png");
        existing.setCreatedAt(new Date(1_700_000_000_000L));
        existing.setState(State.ACTIVE);
        return existing;
    }

    private Category existingCategory() {
        Category category = new Category();
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //category.setId(7L);
        category.setId(ID_7);
        category.setName("beauty");
        category.setDescription("makeup");
        return category;
    }

    @Test
    public void whenProductMissing_thenReturnsNull() {
        /*Arrange: findByIdAndState returns empty for an unknown id*/
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //when(productRepository.findByIdAndState(1L, State.ACTIVE)).thenReturn(Optional.empty());
        when(productRepository.findByIdAndState(ID_1, State.ACTIVE)).thenReturn(Optional.empty());
        ProductDto patch = new ProductDto();
        patch.setName("Lipstick");

        /*Act*/
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //Product result = storageProductService.patchProduct(1L, patch);
        Product result = storageProductService.patchProduct(ID_1, patch);

        /*Assert: controller turns this null into 404 and nothing is written*/
        assertNull(result);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void whenProductIsInactive_thenReturnsNull() {
        /*Arrange: an INACTIVE row is excluded by the State.ACTIVE filter of the lookup*/
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //when(productRepository.findByIdAndState(1L, State.ACTIVE)).thenReturn(Optional.empty());
        when(productRepository.findByIdAndState(ID_1, State.ACTIVE)).thenReturn(Optional.empty());
        ProductDto patch = new ProductDto();
        patch.setName("Lipstick");

        /*Act*/
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //Product result = storageProductService.patchProduct(1L, patch);
        Product result = storageProductService.patchProduct(ID_1, patch);

        /*Assert: 404 for INACTIVE, and no state-blind lookup slipped in*/
        assertNull(result);
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //verify(productRepository).findByIdAndState(1L, State.ACTIVE);
        verify(productRepository).findByIdAndState(ID_1, State.ACTIVE);
        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void whenCategoryDtoHasNoName_thenItIsIgnored() {
        /*Arrange*/
        Category beauty = existingCategory();
        Product existing = existingProduct();
        existing.setCategory(beauty);

        ProductDto patch = new ProductDto();
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setDescription("description without a name"); //no name, must not create a null-named category
        patch.setCategoryDto(categoryDto);

        // OLD (Long id, replaced by UUID, 2026-11-17):
        //when(productRepository.findByIdAndState(1L, State.ACTIVE)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdAndState(ID_1, State.ACTIVE)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        /*Act*/
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //Product result = storageProductService.patchProduct(1L, patch);
        Product result = storageProductService.patchProduct(ID_1, patch);

        /*Assert: product's category is untouched and the category lookup was never reached*/
        assertNotNull(result);
        assertSame(beauty, result.getCategory());
        assertEquals("beauty", result.getCategory().getName());
        assertEquals("makeup", result.getCategory().getDescription());
        verify(categoryRepository, never()).findByName(any());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    public void whenCategoryExistsByName_thenItsDescriptionIsUpdated() {
        /*Arrange: the product currently sits in a different category, so the patch must switch it*/
        Category electronics = new Category();
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //electronics.setId(9L);
        electronics.setId(ID_9);
        electronics.setName("electronics");
        electronics.setDescription("gadgets");

        Category beauty = existingCategory();
        Product existing = existingProduct();
        existing.setCategory(electronics);

        ProductDto patch = new ProductDto();
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("beauty");
        categoryDto.setDescription("new description");
        patch.setCategoryDto(categoryDto);

        // OLD (Long id, replaced by UUID, 2026-11-17):
        //when(productRepository.findByIdAndState(1L, State.ACTIVE)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdAndState(ID_1, State.ACTIVE)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("beauty")).thenReturn(Optional.of(beauty));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        /*Act*/
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //Product result = storageProductService.patchProduct(1L, patch);
        Product result = storageProductService.patchProduct(ID_1, patch);

        /*Assert: same stored row reused, only its description changed*/
        ArgumentCaptor<Product> saved = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(saved.capture());

        assertSame(beauty, result.getCategory());
        assertSame(beauty, saved.getValue().getCategory());
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //assertEquals(7L, saved.getValue().getCategory().getId());
        assertEquals(ID_7, saved.getValue().getCategory().getId());
        assertEquals("beauty", saved.getValue().getCategory().getName());
        assertEquals("new description", saved.getValue().getCategory().getDescription());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    public void whenCategoryExistsAndNoDescriptionSent_thenDescriptionUnchanged() {
        /*Arrange: patch names an existing category but sends no description*/
        Category beauty = existingCategory();
        Product existing = existingProduct();
        existing.setCategory(beauty);

        ProductDto patch = new ProductDto();
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("beauty");
        patch.setCategoryDto(categoryDto);

        // OLD (Long id, replaced by UUID, 2026-11-17):
        //when(productRepository.findByIdAndState(1L, State.ACTIVE)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdAndState(ID_1, State.ACTIVE)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("beauty")).thenReturn(Optional.of(beauty));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        /*Act*/
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //storageProductService.patchProduct(1L, patch);
        storageProductService.patchProduct(ID_1, patch);

        /*Assert: the stored description survives an update that did not mention it*/
        ArgumentCaptor<Product> saved = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(saved.capture());
        assertSame(beauty, saved.getValue().getCategory());
        assertEquals("makeup", saved.getValue().getCategory().getDescription());
    }

    @Test
    public void whenNewCategoryName_thenCategoryCreatedWithActiveState() {
        /*Arrange: the named category does not exist yet*/
        Product existing = existingProduct();
        existing.setCategory(existingCategory());

        ProductDto patch = new ProductDto();
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("toys");
        categoryDto.setDescription("fun");
        patch.setCategoryDto(categoryDto);

        // OLD (Long id, replaced by UUID, 2026-11-17):
        //when(productRepository.findByIdAndState(1L, State.ACTIVE)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdAndState(ID_1, State.ACTIVE)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("toys")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        /*Act*/
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //storageProductService.patchProduct(1L, patch);
        storageProductService.patchProduct(ID_1, patch);

        /*Assert: a brand new category is saved as an ACTIVE row, never with a null state*/
        ArgumentCaptor<Product> saved = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(saved.capture());
        Category created = saved.getValue().getCategory();
        assertNotNull(created);
        assertEquals("toys", created.getName());
        assertNull(created.getId());
        assertEquals(State.ACTIVE, created.getState());
    }

    @Test
    public void whenReplaceHasNoCategory_thenExistingCategoryIsKept() {
        /*Arrange: a PUT without categoryDto must not wipe the product's category*/
        Category beauty = existingCategory();
        Product existing = existingProduct();
        existing.setCategory(beauty);

        ProductDto replacement = new ProductDto();
        replacement.setName("Mascara XL");
        replacement.setDescription("Extra volume");
        replacement.setPrice(new BigDecimal("12.99"));
        replacement.setImageUrl("https://cdn.shopnest/mascara-xl.png");

        // OLD (Long id, replaced by UUID, 2026-11-17):
        //when(productRepository.findByIdAndState(1L, State.ACTIVE)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdAndState(ID_1, State.ACTIVE)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        /*Act*/
        // OLD (Long id, replaced by UUID, 2026-11-17):
        //Product result = storageProductService.replaceProduct(1L, replacement);
        Product result = storageProductService.replaceProduct(ID_1, replacement);

        /*Assert*/
        ArgumentCaptor<Product> saved = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(saved.capture());
        assertSame(beauty, saved.getValue().getCategory());
        assertEquals("beauty", saved.getValue().getCategory().getName());
    }
}
