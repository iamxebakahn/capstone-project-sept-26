package dev.zeba.userauthservice.security;
import dev.zeba.userauthservice.Service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
//for configuration of filters: config chain
//bean of filter; configurable beans : depending on logic of method
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtService jwtService;
    //list of filters i want request to pass through
    //chain of filters we need to define: we need for every request
    //HttpSecurity: object carries configuration, spring gives it and we configure it
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
    //write configuration of all filters that you want
        //do basic configurations: like stateless sessions; disable csrf (cross site req forgery)
        httpSecurity
                .csrf(csrf -> csrf.disable()) //browser with banking page opened, user logged in, hence token & session info is already with browser, user opens new tab and opens malicious link by mistake, this new website might use same session details to send to server, k/a cross site req forgery, bad user from some other site has used user's creds from diff site to make req, chance hackers might send session/csrf tokens hence stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //make session mgmt stateless
                .addFilterBefore(new JwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class)   //filter to be placed b4 which default filter? before usernameauth
                .authorizeHttpRequests(auth -> // 2nd filter: for signup / login we should not ask user to authenticate? no! should be publically available
                        auth.requestMatchers("/auth/**","/error")  //** = anything which comes after it
                                .permitAll()   //permitted to everyone; public, no authentication needed
                                .anyRequest().authenticated() //apart from this all other requests be authenticated
                );

        return httpSecurity.build(); //2 filters in my chain: JwtFilter , authorizeHttpRequests
        // no logic in ctrller for userauth, filter gives userauth; signup able to do it, login will pass too - client gets token -
        //user make another request-> sends token in header of request; valid token - filter 1 allows req to pass, getProducts() filter 2 allows req to pass
        //coz filter itself is removed by spring security ; hence login form is also gone now
    }
}
