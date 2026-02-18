package br.com.gabwsv.secure_todo.controller;

import br.com.gabwsv.secure_todo.service.TaskService;
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
public class AdminTaskController {

    private final TaskService service;

    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')") // Garante que apenas ADMINs executem este metodo
    public ResponseEntity<Void> deleteAllTasks() {
        service.deleteAll();
        return ResponseEntity.noContent().build();
    }
}