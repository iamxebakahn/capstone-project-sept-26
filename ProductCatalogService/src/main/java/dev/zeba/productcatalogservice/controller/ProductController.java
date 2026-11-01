package dev.zeba.productcatalogservice.controller;

import dev.zeba.productcatalogservice.dto.CategoryDto;
import dev.zeba.productcatalogservice.dto.ProductDto;
import dev.zeba.productcatalogservice.dto.ProductPitchDto;
import dev.zeba.productcatalogservice.mappers.ProductMapper;
import dev.zeba.productcatalogservice.model.Product;
import dev.zeba.productcatalogservice.service.IProductService;
import dev.zeba.productcatalogservice.service.PitchService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController //it has @Component inside it, why restcontroller n why not component? : coz we need Spring to do other stuff too
//mapping handler stuff too
@RequestMapping("/products")
public class ProductController {

    // @Autowired //Field injection: not being used here, not autowired constructor injection being used here
    private final IProductService storageService; //Field injection: directly autowiring the field not used in production though, objects added via constructor
    //private DummyProductService dummyProductService; //Dependency Inversion violated - 1 concrete dependent on other concrete
    //to write APIs (function +endpoint)
    //@Component for Spring IOC to create object directly/indirectly : Beans = objects created by Spring IOC, if created by us
    // they are called as Java Objects (Bean is Java object managed by Spring IOC and not programmer)
    //@Autowired to access object
    public ProductController(IProductService storageService, PitchService pitchService, @Qualifier("dummyProductService") IProductService supplierService, IProductService productService, ProductMapper productMapper) {
        this.storageService = storageService;
        this.pitchService = pitchService;
        this.supplierService = supplierService;
        this.productMapper = productMapper;
    }

    private final PitchService pitchService;
    //API to get all products
    //1. make normal java function
    private final IProductService supplierService;
    private final ProductMapper productMapper;
    @GetMapping //attach end point or mapping to it so that users can call it
    List<ProductDto> getAllProducts(){
        //call service layer, repo layer
        List<Product> products = storageService.getAllProducts();
        List<ProductDto> productDtos = new ArrayList<>();
        for(Product product: products){
            productDtos.add(productMapper.toDto(product));
        }
        return productDtos; //1+24 queries, were fired for returning product dtos, coz of 24 categories!
        //by default hibernate doesnot use join instead find main entity then find 2nd entity
        //1 query but hibernate 1+N queries, db efficiency effected - in big relationship creates a problem
        //hence use of @Entity Graph, tells hibernate to fetch which entity while fetching main entity
        //using fetch in join queries in JPQL, also eliminates N to fire 1 query, only fires 1 query
        //in entities relationship, hibernate tries to fetch entities and its relationships but via separate queries
        //to avoid multiple queries shooting, use join fetch
        //in our custom functions, we can mention @Entity Graph to tell hibernate to not fetch relationships via separate queries
    }

    //Path variable - when i/p is in link
    //Ctrler or client layer never directly get/not exposed to entities : due to security issues(hackers can spoof it)
    //if send entire entity: makes APIs slow, send DTO instead(POJO to transfer data, not stored anywhere)
    //if entire object is user taken in i/p and saved in db: ; only business fields are taken and other attributes are based
    //on business logic, clients dont deal with real entities: we make DTOs, hence return Product DTO n't product entity
    //Get rid of /products and use RequestMapping
    @GetMapping("/{id}")
    ResponseEntity<ProductDto> getProductById(@PathVariable("id") Long id){ //PathVariable maps path variable to actual java variable
        if(id<0)
            return new ResponseEntity<>((HttpHeaders) null,HttpStatus.BAD_REQUEST);

        Product product = storageService.getProductById(id);
        //Product product = supplierService.getProductById(id);

        if(product ==null)
            return new ResponseEntity<>((HttpHeaders) null,HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(productMapper.toDto(product), HttpStatus.OK);
    }

    //http response has 3 parts: headers, body/content, status code
    //ResponseEntity : for response body + status code (for web API, response body is default reponse type)

    private  ProductDto from(Product product){
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setImageUrl(product.getImageUrl());

        if(product.getCategory()!=null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setDescription(product.getCategory().getDescription());
            categoryDto.setId(product.getCategory().getId());
            categoryDto.setName(product.getCategory().getName());
            productDto.setCategoryDto(categoryDto);
        }
        return productDto;
    }

    @GetMapping("/{id}/pitch")
    public ResponseEntity<ProductPitchDto> generatePitch(@PathVariable Long id){
        if(id<0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Product product = storageService.getProductById(id);
        if(product == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        ProductPitchDto productPitchDto = new ProductPitchDto();
        productPitchDto.setId(product.getId());
        productPitchDto.setPitch(pitchService.generatePitch(product));
        return new ResponseEntity<>(productPitchDto,HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto){
        Product product = storageService.createProduct(productDto);
        return new ResponseEntity<>(productMapper.toDto(product),HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){

            boolean isDeleted = storageService.deleteProdcutById(id);
            if(isDeleted)
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); //204: i dont have content to return but your request was successful
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> replaceProduct(@PathVariable("id") Long id, @RequestBody ProductDto productDto){
        if(id<0){
            return ResponseEntity.badRequest().build();
        }
        Product product = storageService.replaceProduct(id,productDto);
        if(product == null){
            return ResponseEntity.notFound().build();
        }

        return new ResponseEntity<>(from(product),HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> patchProduct(@PathVariable("id") Long id, @RequestBody ProductDto productDto){
        if(id<0){
            return ResponseEntity.badRequest().build();
        }
        Product product = storageService.patchProduct(id, productDto);
        if(product == null){
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(from(product),HttpStatus.OK);
    }

    @GetMapping("/search")
    //params related to searching go in query params (request params) and not in path variables
    //RequestParams are by default mandatory, but we can set them default = false, if we dont want them
    //search API to give products in price range: i/p min - max price
    //Pageable object allows clients to send page related info: sorting param, size param, page number param
    //PagedModel: contains metadata about pages which client can use to ask for next page, to decide what next action client needs
    //to do (information like how many pages are there, total entries in a page, current page number, next page number client can ask)
    public PagedModel<ProductDto> getProductsInPriceRange(@RequestParam(required = false)BigDecimal minPrice, @RequestParam(required = false)BigDecimal maxPrice,
                                              Pageable pageable){
        Page<ProductDto> productDtos = storageService.getProductsInPriceRange(minPrice,maxPrice,pageable).map(product -> productMapper.toDto(product));
        return new PagedModel<>(productDtos);
        //page number, page size, sort on which attribute : can be passed via pagedmodel
    }
}
