package br.com.gabwsv.secure_todo.controller;

import br.com.gabwsv.secure_todo.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "3. Painel Administrativo", description = "Limpeza de Tasks")
public class AdminTaskController {

    private final TaskService service;

    //VULNERABLE [API05:2023]: Broken Function Level Authorization
    @DeleteMapping("/cleanup")
    @Operation(summary = "Deleta Tasks", description = "Faz uma limpeza na tabela de Tasks.")
    public ResponseEntity<Void> deleteAllTasks() {
        service.deleteAll();
        return ResponseEntity.noContent().build();
    }
}