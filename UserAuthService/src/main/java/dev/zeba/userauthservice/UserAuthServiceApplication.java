package dev.zeba.userauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class UserAuthServiceApplication {
    //Bean : coz method is returning object
    @Bean//by default singleton, not using new keyword in API where it is called, everytime object will be created
    //needs to be shifted to separate security class/config
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static void main(String[] args) {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }

}
