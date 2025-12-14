package br.com.gabwsv.secure_todo.dto.task;

import br.com.gabwsv.secure_todo.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TaskRequestDTO(
        @NotBlank(message = "O título é obrigatório")
        @Size(min=3, max=100, message = "Título deve ter entre 3 a 100 caracteres")
        @Pattern(regexp = "^[^<>]+$", message = "O título não pode conter caracteres HTML (< ou >)")
        String title,

        @Size(max = 500, message = "Descrição muito longa (máx 500)")
        @Pattern(regexp = "^[^<>]+$", message = "A descrição não pode conter caracteres HTML (< ou >)")
        String description,

        TaskPriority priority
) {}
