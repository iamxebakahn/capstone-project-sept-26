package dev.zeba.userauthservice.controller;

import dev.zeba.userauthservice.Service.AuthService;
import dev.zeba.userauthservice.Service.JwtService;
import dev.zeba.userauthservice.dto.AuthRequest;
import dev.zeba.userauthservice.dto.LoginResponse;
import dev.zeba.userauthservice.dto.UserDto;
import dev.zeba.userauthservice.model.Role;
import dev.zeba.userauthservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@Valid @RequestBody AuthRequest authRequest){ //@Valid activates annotations in DTO

        User user = authService.signup(authRequest.getEmail(),authRequest.getPassword());
        if(user == null){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        userDto.setId(user.getId());
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody AuthRequest authRequest){
        String token = authService.login(authRequest.getEmail(),authRequest.getPassword());
        if(token == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        return new ResponseEntity<>(loginResponse,HttpStatus.OK);
    }

    //just for local testing of token, not for frontend flows, never manually but automatic mechanism in spring
    //token always in request header, in postman it has Authorization, same exactly asis in postman
    //JWT token not written in prod, remove it before commiting code
    //once token is put, in header in Authorization, JWT can be seen with starting bearer word otherwise it is unauthorized and has been modified
    //Bearer is automatically added in front of the token, that's how websites/browsers/http client n all are designed
    //if as hacker i get hold of JWT and go on jwt.io, i will not be able to do anything: coz of secret  key
    //if time is v.less i.e. banking token expires fast, social media token: v.high time
    @GetMapping("/validateToken")
    public ResponseEntity<UserDto> validateToken(@RequestHeader("Authorization") String header) {
        if(!header.startsWith("Bearer ")){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try{
            //parsetoken to extract claims: coz it has user info, set user dto and return it
            Claims claims = jwtService.parseToken(header.substring("Bearer ".length()));
            UserDto userDto = new UserDto();
            userDto.setEmail(claims.getSubject());
            userDto.setRole(Role.valueOf(claims.get("role",String.class)));
            return new ResponseEntity<>(userDto,HttpStatus.OK);
        }catch (JwtException e){
            //if exception while passing a token i pass unauthorized
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

    }

    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}

/* user signup -> protected endpoint (login) -> gets token -> access end point (getProduct)-> email/password is not passed on every request,
only token is in auth header of http request -> validate token for a business logic (validate API) -> controller function (getProduct() calls
service layer, but before it validate API would be called!

Problem: repititve code, security concern: if validate token is not called!!! its forgotten! anyone can call anything! manual validation is risky
hence we have security filters!

Filters:
client -> controller (all endpoints) ; before ctrller - we can write filters and spring can write too, before going to ctrller passes through filters
filters: piece of code which runs before request reaches controller endpoint: benefit! : automatically runs for everyrequest, no need of manual validation
filter rejects invalid requests

JWTFilter: validate token, for valid token, role is attached to security object, passes req to next filter, authrules;
Authrules: customer cant delete product from catalogue, auth rules which end point can be called by which role will sit in authrules
after these 2 request goes to controller

instead of manual filters this is preferable:
n no. of filters/chain of filters before controller + default filters run by spring too (eg: random called by user/client - no random in our product: 404 is given back to client)
exception filter: default filter in dispatcher servlet; anonymous user in case auth info: done by anonymous filter
last job of every filter: pass req to next ; hence .dofilter(): last line; if coder forgets to write this line: req is halted at filter 1, doesn't move to next filter


After adding dependencies of security (spring security), getting default filter(.formLogin) (UsernamePasswordAuthenticationFilter)for every endpoint along with basic frontend form: coz of filter, we'll use our own filter to bypass it
need to write our own filters to bypass default filter given by spring
hence login form is also gone now
* */
