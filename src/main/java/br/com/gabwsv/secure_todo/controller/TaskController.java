package br.com.gabwsv.secure_todo.controller;

import br.com.gabwsv.secure_todo.dto.task.TaskRequestDTO;
import br.com.gabwsv.secure_todo.dto.task.TaskResponseDTO;
import br.com.gabwsv.secure_todo.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "2. Tarefas", description = "Gerenciamento de tarefas")
public class TaskController {

    private final TaskService service;

    @Operation(summary = "Criar nova tarefa", description = "Cria uma tarefa vinculado ao usuário logado.")
    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@RequestBody @Valid TaskRequestDTO request){
        return ResponseEntity.ok(service.createTask(request));
    }

    @Operation(summary = "Listar minhas tarefas", description = "Retorna apenas as tarefas do usuário logado")
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> listAll(){
        return ResponseEntity.ok(service.findAllMyTasks());
    }

    @Operation(summary = "Deletar tarefa", description = "Remove uma tarefa. Impede que um usuário B delete uma tarefa do usuário A (IDOR).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Upload de Anexo", description = "Envia arquivos para a tarefa. Valida extensão e Conteúdo real (Magic Bytes). Aceita: PDF, JPEG, JPG, PNG."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload realizado"),
            @ApiResponse(responseCode = "403", description = "Arquivo malicioso ou permissão negada."),
            @ApiResponse(responseCode = "500", description = "Erro interno (Spoofing detectado)")
    })
    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@PathVariable UUID id, @RequestParam("file")MultipartFile file){
        service.uploadAttachment(id, file);
        return ResponseEntity.ok("Arquivo enviado com sucesso!");
    }

    @Operation(summary = "Atualizar Tarefa.", description = "Atualiza as informações da tarefa (PUT)")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid TaskRequestDTO request){
        return ResponseEntity.ok(service.updateTask(id, request));
    }

    @Operation(summary = "Alterar status.", description = "Marca a tarefa como CONCLUIDA ou PENDENTE. (PATCH)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDTO> updateStatus(@PathVariable UUID id){
        return ResponseEntity.ok(service.updateTaskStatus(id));
    }
}
