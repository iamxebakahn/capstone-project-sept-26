package dev.zeba.productcatalogservice.security;

import dev.zeba.productcatalogservice.dto.AuthUserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenValidationFilter extends OncePerRequestFilter {

    private final String authServiceUrl; //to read authUrl
    private final RestTemplate restTemplate; //for rest template object

    public TokenValidationFilter(@Value("${auth.service.url}")String authServiceUrl,
                                 RestTemplateBuilder restTemplateBuilder) {
        this.authServiceUrl = authServiceUrl;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //all get end points to be public - no authentication
        //any role can do post/put - need to be authenticated (valid token with any role)
        //delete - admin role only
        if (request.getMethod().equals("GET")) {
            // no need of security context verification/validation: pass request to next filter
            filterChain.doFilter(request, response);
            return;
        }

        //if not get req, call authservice to validate token
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        AuthUserDto authUserDto;
        //if valid header call authservice
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", header);
            //can use get (coz of /get in authcontroller )but using excehange
            ResponseEntity<AuthUserDto> answer =
                    restTemplate.exchange(this.authServiceUrl + "/auth/validateToken", HttpMethod.GET, new HttpEntity<>(headers), AuthUserDto.class);
            authUserDto = answer.getBody();
        } catch (HttpClientErrorException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());//token is invalid
            return;
        } catch (RestClientException e) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value()); //service not working/service is down
            return;
        }

        //response/req (by default empty) client sends -> call to authservice -> answer returned by authservice -> based on answer status in response is to be set
        //2 ways to interact with microservice: syn: rest template/fienclient direct call w/o response
        //asynch: kafka

        if (request.getMethod().equals("DELETE") && !authUserDto.getRole().equals("ADMIN")) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }
        //attach filter in front of request coming to productcatalog; no manual token validation call authservice api for token validation
        //Tomcat runs on 8080, both services(productcatalog and authservice) run on 8080! 1 port listens to 1 request hence productcatalog on 8080
        //authservice shift to 8081 (server.port=8081 added to properties in userauth)
        //connect to service with its URL ; add in application properties

        filterChain.doFilter(request, response);

        //authservice is handling authentication of which entity in system? user hence authservice is called identity service
        //business logic not owned by authservice only token validation is owned by authservice
    }
}
