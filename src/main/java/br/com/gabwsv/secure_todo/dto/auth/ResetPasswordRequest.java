package br.com.gabwsv.secure_todo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest (
        @NotBlank @Email String email,
        @NotBlank @Size(min= 4, max=4) String code,
        @NotBlank @Size(min = 8, max = 50)
        String newPassword
){}
