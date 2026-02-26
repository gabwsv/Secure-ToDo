package br.com.gabwsv.secure_todo.controller;

import br.com.gabwsv.secure_todo.dto.task.TaskImportRequest;
import br.com.gabwsv.secure_todo.dto.task.TaskRequestDTO;
import br.com.gabwsv.secure_todo.dto.task.TaskResponseDTO;
import br.com.gabwsv.secure_todo.service.EmailService;
import br.com.gabwsv.secure_todo.service.RateLimiterService;
import br.com.gabwsv.secure_todo.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "2. Tarefas", description = "Gerenciamento de tarefas")
public class TaskController {

    private final TaskService service;
    private EmailService emailService;
    private RateLimiterService invitationRateLimiter;
    private RestTemplate restTemplate;

    @Operation(summary = "Criar nova tarefa", description = "Cria uma tarefa vinculado ao usuário logado.")
    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@RequestBody @Valid TaskRequestDTO request){
        return ResponseEntity.ok(service.createTask(request));
    }

    @Operation(summary = "Criar uma lista de tarefas", description = "Cria uma lista de tarefas vinculado ao usuário logado.")
    @PostMapping("/batch")
    public ResponseEntity<?> createTasks(@RequestBody @Size(max=50, message= "Limite de 50 tasks por vez")
                                             List<@Valid TaskResponseDTO> tasks){
        service.createTasks(tasks);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Listar minhas tarefas", description = "Retorna apenas as tarefas do usuário logado")
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> listAll(){
        return ResponseEntity.ok(service.findAllMyTasks());
    }

    @Operation(summary = "Recuperar tarefa", description = "Retorna uma tarefa a partir do seu UUID")
    @GetMapping("{id}")
    public ResponseEntity<TaskResponseDTO> getTask(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getTaskById(id));
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

    @PostMapping("/{taskId}/invite")
    @Operation(summary = "Convidar colaborador", description = "Permite adicionar outro usuário à task. Protegido por Rate Limit.")
    public ResponseEntity<?> inviteCollaborator(@PathVariable UUID taskId, @RequestParam String email) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        // Proteção contra automação (API06)
        if (invitationRateLimiter.isQuotaExceeded(currentUser)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Limite de convites atingido. Tente novamente mais tarde.");
        }

        if(service.isAlreadyCollaborator(taskId, email)){
            return ResponseEntity.badRequest().body("Usuário já colabora nesta tarefa.");
        }

        service.addCollaborator(taskId, email);
        //emailService.sendInvitation(email, taskId); // Opcional

        return ResponseEntity.ok("Colaborador adicionado com sucesso.");
    }

    @PostMapping("/import")
    @Operation(summary = "Importar tarefas", description = "Importar tarefas de uma URL externa confiável. Proteção contra SSRF aplicada")
    public ResponseEntity<?> importTasks(@RequestBody @Valid TaskImportRequest request) {
        service.importTasksFromUrl(request);
        return ResponseEntity.ok("Processo de importação iniciado com sucesso.");
    }

    @PostMapping("/{id}/enrich")
    @Operation(summary = "Adicionar dica motivacional", description = "Consome uma API externa para adicionar uma dica à tarefa.")
    public ResponseEntity<TaskResponseDTO> enrich(@PathVariable UUID taskId) {
        return ResponseEntity.ok(service.enrichTaskWithTip(taskId));
    }
}
