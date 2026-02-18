package br.com.gabwsv.secure_todo.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateDTO(
        @NotBlank String nome,
        @NotBlank String username
        // Não incluímos 'role' ou 'saldo'  aqui
) {}
