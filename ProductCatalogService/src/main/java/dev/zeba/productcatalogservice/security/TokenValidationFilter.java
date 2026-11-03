package dev.zeba.productcatalogservice.security;

import org.springframework.web.filter.OncePerRequestFilter;

public class TokenValidationFilter extends OncePerRequestFilter {
    //attach filter in front of request coming to productcatalog; no manual token validation call authservice api for token validation
    //Tomcat runs on 8080, both services(productcatalog and authservice) run on 8080! 1 port listens to 1 request hence productcatalog on 8080
    //authservice shift to 8081


}
