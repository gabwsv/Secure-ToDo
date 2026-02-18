package br.com.gabwsv.secure_todo.dto.task;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record TaskImportRequest(
        @NotBlank(message = "A URL é obrigatória")
        @URL(message = "Formato de URL inválido")
        String url
) {}
