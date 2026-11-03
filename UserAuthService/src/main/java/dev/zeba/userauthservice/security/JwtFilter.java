package dev.zeba.userauthservice.security;

import dev.zeba.userauthservice.Service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter { //every req goes through this filter once hence OncePerRequestFilter
    private final JwtService jwtService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) // why chain? to call next filter
            throws ServletException, IOException {
        //write logic of passing token
        String header = request.getHeader("Authorization");
                if (header != null && header.startsWith("Bearer ")) {

                    try {
                        Claims claims = jwtService.parseToken(header.substring("Bearer ".length()));
                        //set roles inside securitycontextholder
                        //GrantedAuthority: spring's way of knowing what is user's role (Role is stored as enums/stings in db: security understands granted authority n not enums/string)
                        List<GrantedAuthority> grantedAuthorities = List.of(
                                new SimpleGrantedAuthority("ROLE_" + claims.get("role",String.class)) //convert enum to granted authority ; multiple entries for multiple roles of user
                        ); //ROLE_ coz spring convention to create obj of grantedauthority should begin with ROLE_ : forget to write ROLE_ : req will fail at filter 2, coz filter 2 expects ROLE_ADMIN and not ADMIN from filter 1
                        Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, grantedAuthorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } catch (JwtException e) {
                        //opt 1: token passing fails, reject req from filter1-> problem public end points cannot pass then, into filter 2 if req is filtered out filter 1
                        //permitAll() which excepts public req /signup, /login will not have token, parsetoken function will fail!, : public end point will not even paas
                        //opt 2: check if  its public URL // lots of checks no!!!
                        //if parseing fails, context holder should have empty in auth info, then goes to next end point, public enpoint will allow it to pass, but protected will
                        //not allow it to pass
                        //set my security context holder as empty so it passes through public end point and filter 2 will stop the unauthorized ones
                        SecurityContextHolder.clearContext();
                    }
                }
        //every security filter should have security filter context (compacted information/reference)object
                //security filter context: roles, username, request validated or not? client (req) ->JWTFilter(obj of security context) -> filter 1
                //so that other filters know JWT has already authenticated req, other filters need to work on their own thing
                //every filter might update context, so as developer custom filters need to be written with logic to update security context
                //Authentication attribute only is concern of JWTFilter (rest attributes are not JWTFilters concern)

              filterChain.doFilter(request,response); //skip this line and req will halt at filter 1
                //create a protected end point and test it!
    }
}
