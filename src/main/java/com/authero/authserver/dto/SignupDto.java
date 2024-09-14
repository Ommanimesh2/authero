package com.authero.authserver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignupDto {
    @Email(message = "email must be valid")
    private String email;
    @NotNull(message = "password cannot be null")
    private String password;
    @NotNull(message = "fullName cannot be null")
    private String fullName;
}
