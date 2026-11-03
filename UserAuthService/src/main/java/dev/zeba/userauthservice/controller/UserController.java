package dev.zeba.userauthservice.controller;

import dev.zeba.userauthservice.dto.UserDto;
import dev.zeba.userauthservice.model.User;
import dev.zeba.userauthservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepo;
    @GetMapping("/me")
    //req hitting this endpoint : passed all filters; authentication object we can have now
    public ResponseEntity<UserDto> me(Authentication authentication) {
        Optional<User> optionalUser = userRepo.findByEmail(authentication.getName());
        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        UserDto dto = new UserDto();
        dto.setId(optionalUser.get().getId());
        dto.setEmail(optionalUser.get().getEmail());
        dto.setRole(optionalUser.get().getRole());
        return ResponseEntity.ok(dto);
    }

    /*
    Different scenarios to test
    +ve, -ve, edge, expired token
    error 403: filter 1 passed, issue in filter 2, authrequest
    after timer expires for token, filter 2 will reject it and error will be 403

       Controller Advice: if service throws error, writing try catch block for same exception is not possible, central class where all exceptions
       are handled is created
    * */
}
