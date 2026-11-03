package dev.zeba.productcatalogservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.service.annotation.GetExchange;

@Getter
@Setter
public class AuthUserDto {
    //to communicate with external service
    private String email;
    private String role;
}

//attach filter in front of request coming to productcatalog; no manual token validation call authservice api for token validation