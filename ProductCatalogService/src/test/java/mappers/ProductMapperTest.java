package mappers;

import dev.zeba.productcatalogservice.dto.ProductDto;
import dev.zeba.productcatalogservice.mappers.ProductMapper;
import dev.zeba.productcatalogservice.model.Category;
import dev.zeba.productcatalogservice.model.Product;
import dev.zeba.productcatalogservice.model.enums.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ProductMapperTest {

    Product product;

    @BeforeEach //becomes 1st line before any function within this class is executed ;
    public void setup() {
        product = new Product();
    }

    @Test
    public void testWhenValidEntity_thenReturnValidDto() {
        //Arrange
        ProductMapper productMapper = new ProductMapper();
        Category category = new Category();
        category.setName("beauty");
        //Product product = new Product();
        product.setId(1L);
        product.setName("Mascara");
        product.setPrice(new BigDecimal("9.99"));
        product.setCategory(category);

        //Act
        ProductDto productDto = productMapper.toDto(product);

        //Assert: Checking on converting to Dto, values are not corrupted
        assertEquals(productDto.getId(), 1L);
        assertEquals(productDto.getName(), "Mascara");
        assertEquals(productDto.getPrice(), new BigDecimal("9.99"));
        assertEquals(productDto.getCategoryDto().getName(), "beauty");
    }

    @Test
    public void whenCategoryNotMapped_thenIsNull() {
        /*Arrange also the setup function */
        ProductMapper productMapper = new ProductMapper();
        //Product product = new Product(); //this line is going to be same in most of the test cases: solution BeforeEach
        product.setName("Mascara"); //not setting the category, hence category dto's got to be null

        /*Act */
        ProductDto productDto = productMapper.toDto(product);
        //if assert is not written/forgotten, test always passes
        /*Assert */
        assertNull(productDto.getCategoryDto());
    }

    private Product existingProduct() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setName("Mascara");
        existing.setDescription("Volumising mascara");
        existing.setPrice(new BigDecimal("9.99"));
        existing.setImageUrl("https://cdn.shopnest/mascara.png");
        existing.setCreatedAt(new Date(1_700_000_000_000L));
        existing.setState(State.ACTIVE);
        return existing;
    }

    @Test
    public void whenOnlyNameSent_thenOnlyNameChanges() {
        /*Arrange*/
        ProductMapper productMapper = new ProductMapper();
        Product existing = existingProduct();
        ProductDto patch = new ProductDto();
        patch.setName("Lipstick");

        /*Act*/
        Product result = productMapper.applyPatch(existing, patch);

        /*Assert: only the sent field moved*/
        assertEquals("Lipstick", result.getName());
        assertEquals("Volumising mascara", result.getDescription());
        assertEquals(new BigDecimal("9.99"), result.getPrice());
        assertEquals("https://cdn.shopnest/mascara.png", result.getImageUrl());
    }

    @Test
    public void whenOnlyPriceSent_thenOnlyPriceChanges() {
        /*Arrange*/
        ProductMapper productMapper = new ProductMapper();
        Product existing = existingProduct();
        ProductDto patch = new ProductDto();
        patch.setPrice(new BigDecimal("12.50"));

        /*Act*/
        Product result = productMapper.applyPatch(existing, patch);

        /*Assert: only the sent field moved*/
        assertEquals(new BigDecimal("12.50"), result.getPrice());
        assertEquals("Mascara", result.getName());
        assertEquals("Volumising mascara", result.getDescription());
        assertEquals("https://cdn.shopnest/mascara.png", result.getImageUrl());
    }

    @Test
    public void whenEmptyDtoSent_thenNothingChanges() {
        /*Arrange*/
        ProductMapper productMapper = new ProductMapper();
        Product existing = existingProduct();
        ProductDto patch = new ProductDto();

        /*Act*/
        Product result = productMapper.applyPatch(existing, patch);

        /*Assert: absent fields are null in the DTO and must be left untouched*/
        assertEquals("Mascara", result.getName());
        assertEquals("Volumising mascara", result.getDescription());
        assertEquals(new BigDecimal("9.99"), result.getPrice());
        assertEquals("https://cdn.shopnest/mascara.png", result.getImageUrl());
    }

    @Test
    public void whenPatchSent_thenIdCreatedAtAndStateNeverChange() {
        /*Arrange*/
        ProductMapper productMapper = new ProductMapper();
        Product existing = existingProduct();
        Date createdAt = existing.getCreatedAt();
        ProductDto patch = new ProductDto();
        patch.setId(99L); //client-supplied id must be ignored
        patch.setName("Lipstick");
        patch.setDescription("Matte lipstick");
        patch.setPrice(new BigDecimal("12.50"));
        patch.setImageUrl("https://cdn.shopnest/lipstick.png");

        /*Act*/
        Product result = productMapper.applyPatch(existing, patch);

        /*Assert*/
        assertSame(existing, result);
        assertEquals(1L, result.getId());
        assertSame(createdAt, result.getCreatedAt());
        assertEquals(State.ACTIVE, result.getState());
    }
}

    //Aftereach: method which runs after every test case, used for cleaning up things; eg file consumed by test cases,
    //test 1 runs, make changes to file, so aftereach gets rid of file (refresh file, cleaning up of resources, resetting)and makes use of the file in test 2 without alterations made
    //by test 2
    //to track my frontend is working fine, all 24*7, GUI automations are there: eg. pollynator continously scrolls after every
    //interval of time if everything is fine on frontend: eg. pollynator bots  checks every 15 min my submit button is working fine on frontend
    //authentication / authorization: JWT
    //2012: linkedin password stole: 60% password cracked coz password/linkedin hashing algorithm not made with salt
    //storing password: v.imp: security issue
    //how is password stored:
    //approach 1: store as is: user's data stored as is: no way
    //approach 2: encode it: maintain mapping: character conversion map is still not safe, reverse mapping possible
    //approach 3: encrypt: little safer than 1/2: algo generates gibberish in place of data: however problem is decryption possible
    //if anyone has key of data, coz its a 2 way process
    //approach 4: hashing: benefit over encryption: 1 way process: but still problem: hashing function problem is lot of people will
    //have similar hashes (ex. h(x%10): 100, 200, 50 : 0,0,0 : same hash! at scale easy for hackers to get an idea of password
    //approach 5: hashing with a salt: (salt: random value added to hashing algo for every user: hence 2 users with same password
    // will have different salt): used in prod, can't be reversed
    //Bcrypt: hackers can crack 5 too. Bcrypt uses multiple algos. Can't be reversed. requires 60ms to crack 1 password.
    //if complexity cant prevent hacking, make computation slow/heavy. Benefit: till the time hackers will crack everything,
    //in good company alert will be raised in this duration
    //slow algo needed here


//Lifecycle:
//signup for account: 2 main attributes: email/password; stored in db
//next time use same email/password (id: email: password after hashing)
//how it checks if password is same? option 1: converts user given password into hash using same algo and  compare it with value
// in db, option 2: gets value from db, converts it into original password and compare with user input
//option 1 is done, option 2: not possible, dehashing is not possible, cant convert password back to plain text, leave matching
//Rest APIs dont store state, hence backend is to be told at every request which is the user, login info is to be sent at every request
// one method: ask user to login after every action: ex. hotstar would not ask for login, if i need to watch video 2 after i watched video 1
// i.e. server needs better way to remember user, cant expect user to send email/password on every request
//server remembers user with better way : token; before token era: sever site sessions or sessions: still in use in some places
//session: server remembering some info about user,(login : backend notebook(RAM, db, cache) - server  writes info in this storage/notebook)
//lots of metadata about user is maintained in this notebook by server - backend sends session id in response and browser stores
//this session id and with every request it associates this session id and server then verifies session id with stored info on
//every new request: benefit of making server hold user info: when user is gone, server becomes the owner and invildates the session
// without users intervention and user will have to login again and will not be able to reuse the same session
//disdvantage: data breach: if hackers get hold on notebook, problem arises: apps runs on multiple servers, hence notebook needs
// to be shared with each machine: if one server invalidates entry, all servers need to be informed immediately
//Token: login for first time, server saves login, validates email pass, if everything is fine, server sends token in reposnse
// which is stored by browser in cookies and next time, browser sends same token, server verifies if token is valid or not
//server doesnt store anything now, it just runs algo and checks if token is valid or not
//most fsamous tech for token in backend: JWT: Jason Web Token
//authentication: who are you autherization: what can you do
//JWT: 3 parts: header: algo used to sign token, so server can use same algo to verify token
// payload: other metadata of login, email, role, when toke was issued (iat), who issued it, claims:extra info added to token(eg: role)
// signature: tells authenticity of token, no one has modified/spoofed it (eg. hacker changes read user to admin/something)
//hacker can change signature too? to match role but signature is generated using payload + header + secretkey then is passed through
//hashing algo like SHA256 or something else and then a signature is generated (Secret only server knows) : secret key is used to sign the token
// these 3 components are sent in token: how? encrypted/hashed/encoded? Base64 URL encoded: Hacker can easily read them:
//info never to be sent in JWT: password: coz hackers can read them
//if i have someone's JWT token, i can make request on someone's behalf; hence expiration to expire/invalidate JWT tokens (Banking website: very short time can remain on)
//even if i have some1's token, i can do same things, nothing extra on some1's behalf, coz i cant change signatature! coz secret key
//component of signature is in server
//SpringSecutrity not added, implementing JWT : coz public APIs would have also gotten locked because of it! SpringSecurity has
//default behavious of locking all endpoints, even public
//Dependencies: spring-security-crypto for bcrypt crypto algo, JWT API, JWT IMPL, JWT Jackson