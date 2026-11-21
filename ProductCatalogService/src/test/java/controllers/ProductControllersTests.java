package controllers;
import dev.zeba.productcatalogservice.ProductCatalogServiceApplication;
import org.springframework.test.context.ContextConfiguration;


import dev.zeba.productcatalogservice.clients.AuthClient;
import dev.zeba.productcatalogservice.controller.ProductController;
import dev.zeba.productcatalogservice.mappers.ProductMapper;
import dev.zeba.productcatalogservice.model.Product;
import dev.zeba.productcatalogservice.service.IProductService;
import dev.zeba.productcatalogservice.service.PitchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@ContextConfiguration(classes =     ProductCatalogServiceApplication.class)
@Import(ProductMapper.class) //not an external class, its just a func call, normal java call in separate class, import productmapper rather than create mockobject: real obj is inserted and not dummy obj
public class ProductControllersTests {
    @Autowired
    private MockMvc mockMvc; //like a mockclient in service and repo, mock beans and injecting in testing class obj, but this time, for controller, webmvc is creating obj too

    @MockitoBean// when obj is automatically created and there's no way of injecting mock , we create mockito bean : creates obj + injects mocks too automatically
    private IProductService productService;

    @MockitoBean // creates mockbean + injects it too, no need to write injectbean
    private PitchService pitchService;

    @MockitoBean
    private AuthClient authClient;
    //in non controller tests - spring context was not being set up, in CI/CD pipelines - some tests take time to execute like this: spinning up entire spring context
    @Test // in non controller - test was calling function directly, here call endpoint not the func, json => i/p & o/p ; get runs w/o authentication so would pass easily
    void getProductById_existingProduct_returns200AndTheJson() throws Exception { //authclient bean is needed to set up token filter, w/o authbean, error is thrown even when get works w/o authbean
        UUID id = UUID.randomUUID();
        Product product = new Product();
        product.setId(id);
        product.setName("Mascara");
        product.setPrice(new BigDecimal("9.99"));
        when(productService.getProductById(id)).thenReturn(product);

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Mascara"))
                .andExpect(jsonPath("$.price").value(9.99));
    }

    @Test
    void createProduct_noToken_returns401AndNothingIsSaved() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mascara\"}"))
                .andExpect(status().isUnauthorized());

        // verify request getting rejected at filter level itself, make sure it is not calling the method itself after the req is rejected at the filter itself, should not touch controller level
        verify(productService, never()).createProduct(any());
    }
}
