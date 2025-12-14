package br.com.gabwsv.secure_todo.service;

import br.com.gabwsv.secure_todo.dto.task.TaskRequestDTO;
import br.com.gabwsv.secure_todo.dto.task.TaskResponseDTO;
import br.com.gabwsv.secure_todo.enums.TaskPriority;
import br.com.gabwsv.secure_todo.enums.TaskStatus;
import br.com.gabwsv.secure_todo.model.Task;
import br.com.gabwsv.secure_todo.model.User;
import br.com.gabwsv.secure_todo.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final FileStorageService fileStorageService;

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

    public TaskResponseDTO updateTask(UUID id, TaskRequestDTO request){
        User user = getLoggedUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));

        if(!task.getUser().getId().equals(user.getId())){
            throw new SecurityException("Você não tem permissão para alterar esta tarefa.");
        }

        if(request.title() != null)
            task.setTitle(request.title());
        if(request.description() != null)
            task.setDescription(request.description());
        if(request.priority() != null)
            task.setPriority(request.priority());

        taskRepository.save(task);
        return TaskResponseDTO.fromEntity(task);
    }

    public TaskResponseDTO updateTaskStatus(UUID id){
        User user = getLoggedUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada."));

        if(!task.getUser().getId().equals(user.getId())){
            throw new SecurityException("Permissão negada.");
        }

        if(TaskStatus.PENDENTE.equals(task.getStatus()))
            task.setStatus(TaskStatus.CONCLUIDO);
        else
            task.setStatus(TaskStatus.PENDENTE);

        taskRepository.save(task);
        return TaskResponseDTO.fromEntity(task);

    }

    // ---- Listar "minhas" tarefas ----
    public List<TaskResponseDTO> findAllMyTasks(){
        User user = getLoggedUser();

        return taskRepository.findByUser(user).stream()
                .map(TaskResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void deleteTask(UUID id){
        User user = getLoggedUser();

        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (!task.getUser().getId().equals(user.getId())){
            throw new SecurityException("Você não tem permissão para deletar esta tarefa.");
        }

        taskRepository.delete(task);
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

}
