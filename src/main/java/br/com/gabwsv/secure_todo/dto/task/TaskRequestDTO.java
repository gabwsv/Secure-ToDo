package br.com.gabwsv.secure_todo.dto.task;

import br.com.gabwsv.secure_todo.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TaskRequestDTO(
        @NotBlank(message = "O título é obrigatório")
        //Sem sanitização
        String title,

        String description,

        TaskPriority priority
) {}
