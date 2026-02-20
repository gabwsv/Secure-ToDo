package br.com.gabwsv.secure_todo.controller;

import br.com.gabwsv.secure_todo.model.Task;
import br.com.gabwsv.secure_todo.repository.TaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "4. Old Task", description = "Gerenciamento de tarefas depreciado")
//VULNERABLE [API09:2023]: Improper Inventory Management
//@Deprecated // Sinaliza para outros desenvolvedores
public class OldTaskController {

    private final TaskRepository taskRepository;

    public OldTaskController(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }

    @GetMapping
    @Operation(summary = "Exemplo", description = "Método depreciado.")
    public ResponseEntity<?> list() {
        return ResponseEntity.status(HttpStatus.GONE) // Retorna 410 (Gone)
                             .body("Esta versão da API foi descontinuada. Use a V2.");
    }

    @GetMapping("/debug/latest")
    @Operation(summary = "Endpoint de Debug antigo", description = "Retorna as ultimas Tasks criadas, para fins de teste.")
    public ResponseEntity<List<Task>> listLatestTasks(){
        //FALHA CRITICA: Retorna a entidade completa, de todos os usuários sem validações de segurança.
        return ResponseEntity.ok(taskRepository.findAll());
    }

    // ... métodos antigos sem as novas travas de segurança
}






