package br.com.gabwsv.secure_todo.service;

import br.com.gabwsv.secure_todo.dto.task.ExternalTipDTO;
import br.com.gabwsv.secure_todo.dto.task.TaskImportRequest;
import br.com.gabwsv.secure_todo.dto.task.TaskRequestDTO;
import br.com.gabwsv.secure_todo.dto.task.TaskResponseDTO;
import br.com.gabwsv.secure_todo.enums.TaskPriority;
import br.com.gabwsv.secure_todo.enums.TaskStatus;
import br.com.gabwsv.secure_todo.model.Task;
import br.com.gabwsv.secure_todo.model.User;
import br.com.gabwsv.secure_todo.repository.TaskRepository;
import br.com.gabwsv.secure_todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private final FileStorageService fileStorageService;
    private final RestClient restClient;

    // ---- Criar TAREFA ----
    public TaskResponseDTO createTask(TaskRequestDTO request){
        User user = getLoggedUser();

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority() != null ? request.priority() : TaskPriority.MEDIA)
                .user(user)
                .build();
        taskRepository.save(task);
        return TaskResponseDTO.fromEntity(task);
    }

    //VULNERABLE [API04:2023]
    public void createTasks(List<TaskRequestDTO> dtos) {
        User user = getLoggedUser();

        List<Task> tasks = dtos.stream()
                .map(dto -> Task.builder()
                                        .title(dto.title())
                                        .description(dto.description())
                                        .priority(dto.priority() != null ? dto.priority() : TaskPriority.MEDIA)
                                        .user(user)
                                        .build())
                                .toList();

        taskRepository.saveAll(tasks);
    }


    //VULNERABLE: [API01:2023] BOLA - Sem checagem do proprietario do objeto.
    public TaskResponseDTO updateTask(UUID id, TaskRequestDTO request){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));

        if(request.title() != null)
            task.setTitle(request.title());
        if(request.description() != null)
            task.setDescription(request.description());
        if(request.priority() != null)
            task.setPriority(request.priority());

        taskRepository.save(task);
        return TaskResponseDTO.fromEntity(task);
    }

    //VULNERABLE: [API01:2023] BOLA - Sem checagem do proprietario do objeto.
    public TaskResponseDTO updateTaskStatus(UUID id){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));

        if(TaskStatus.PENDENTE.equals(task.getStatus()))
            task.setStatus(TaskStatus.CONCLUIDO);
        else
            task.setStatus(TaskStatus.PENDENTE);

        taskRepository.save(task);
        return TaskResponseDTO.fromEntity(task);

    }

    //VULNERABLE: [API01:2023] BOLA - Sem checagem do proprietario do objeto.
    public void deleteTask(UUID id){
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        taskRepository.delete(task);
    }

    //VULNERABLE: [API01:2023] BOLA - Sem checagem do proprietario do objeto.
    public TaskResponseDTO getTaskById(UUID idTask) {
        return taskRepository.findById(idTask)
                .map(TaskResponseDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Task não encontrada"));
    }

    public List<TaskResponseDTO> findAllMyTasks(){
        User user = getLoggedUser();

        return taskRepository.findByUser(user).stream()
                .map(TaskResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }





    // ---- Metodo auxiliar para pegar usuário logado ----
    public User getLoggedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    public void uploadAttachment(UUID taskId, MultipartFile file){
        User user = getLoggedUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));

        if(!task.getUser().getId().equals(user.getId())){
            throw new SecurityException("Você não tem permissão para alterar esta tarefa.");
        }

        String fileName = fileStorageService.storedFile(file);

        task.setAttachmentPath(fileName);

        taskRepository.save(task);
    }

    public void deleteAll() {

    }

    public void updateDescription(Long taskId, String dica) {
    }

    //VULNERABLE [API10:2023]: Unsafe Consumption of APIs
    public TaskResponseDTO enrichTaskWithTip(UUID taskId){
        User user = getLoggedUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        //API01: Validar se a tarefa pertence ao usuário
        if (!task.getUser().getId().equals(user.getId())){
            throw new SecurityException("Acesso negado.");
        }

        try{
            //Consumo Seguro (API10): RestClient já possui timeouts de rede
            ExternalTipDTO response = restClient.get()
                    .uri("/v1/productivity-tip")
                    .retrieve()
                    .body(ExternalTipDTO.class);

            //Lógica vulneravel por excesso de confiança
            if(response != null && response.tip() != null){
                task.setDescription(task.getDescription() + "\n\nTip: "+ response.tip());
                taskRepository.save(task);
            }
        } catch (Exception e){
            System.out.println("Falha ao obter dica externa: {}" + e.getMessage());
        }
        return  TaskResponseDTO.fromEntity(task);
    }

    public void addCollaborator(UUID taskId, String collaboratorEmail){
        User user = getLoggedUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));

        //Segurança (API01): Apenas o dono pode convidar
        if(!task.getUser().getId().equals(user.getId())){
            throw new SecurityException("Apenas o propietário pode convidar colaboradores.");
        }

        User collaborator = userRepository.findByEmail(collaboratorEmail)
                .orElseThrow(() -> new RuntimeException("Usuário convidado não encontrado."));

        //Evitar duplicados e convidar a si mesmo
        if (task.getUser().equals(collaborator) || task.getCollaborators().contains(collaborator)){
            throw new RuntimeException("Convite inválido ou usuário já é colaborador.");
        }

        task.getCollaborators().add(collaborator);
        taskRepository.save(task);
    }

    public boolean isAlreadyCollaborator(UUID taskId, String email){
        return taskRepository.findById(taskId)
                .map(t -> t.getCollaborators().stream()
                        .anyMatch(c -> c.getUsername().equals(email)))
                .orElse(false);
    }

    //VULNERABLE [API07:2023]: Server Side Request Forgery(SSRF)
    public void importTasksFromUrl(TaskImportRequest request){
        String url = request.url();

        try {
            // RestClient já configurado com timeouts no ApplicationConfig
            String jsonContent = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            //Lógica para converter o JSON em Tasks e salvar
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

}
