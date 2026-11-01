package dev.zeba.userauthservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthRequest {
    @NotBlank
    @Email
    private String email;

    @Size(min = 8)
    @NotBlank
    private String password;
}
