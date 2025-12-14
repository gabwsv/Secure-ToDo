package br.com.gabwsv.secure_todo.dto.auth;

import br.com.gabwsv.secure_todo.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "O usuário é obrigatório")
        @Size(min=3, max=30, message="Usuário deve ter entre 3 a 30 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Nome de usuário deve conter apenas letras, números e underline")
        String username,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min=8, max=50, message = "A senha deve ter no mínimo 8 caracteres")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                 message = "Senha fraca: deve ter maiúscula, minúscula, número e caractere especial")
        String password,
        @NotNull(message = "O papel (role) é obrigatório")
        UserRole role
) {}
