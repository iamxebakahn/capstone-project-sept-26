package dev.zeba.userauthservice.dto;

import dev.zeba.userauthservice.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String email;
    private Role role;
}
