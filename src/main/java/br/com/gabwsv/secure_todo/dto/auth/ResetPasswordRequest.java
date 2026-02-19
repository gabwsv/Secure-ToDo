package br.com.gabwsv.secure_todo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest (
        @NotBlank @Email String email,
        @NotBlank @Size(min= 6, max=6) String code,
        @NotBlank @Size(min = 8, max = 50)
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Senha não segue política de senha segura")
        String newPassword
){}
