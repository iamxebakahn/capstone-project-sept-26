package dev.zeba.userauthservice.controller;

import dev.zeba.userauthservice.Service.AuthService;
import dev.zeba.userauthservice.dto.AuthRequest;
import dev.zeba.userauthservice.dto.UserDto;
import dev.zeba.userauthservice.model.Role;
import dev.zeba.userauthservice.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@Valid @RequestBody AuthRequest authRequest){

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
}
