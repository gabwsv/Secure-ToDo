package br.com.gabwsv.secure_todo.dto.task;

import br.com.gabwsv.secure_todo.enums.TaskPriority;
import br.com.gabwsv.secure_todo.enums.TaskStatus;
import br.com.gabwsv.secure_todo.model.Task;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        TaskPriority priority,
        TaskStatus status,
        LocalDateTime createdAt
) {
    public static  TaskResponseDTO fromEntity(Task task){
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getCreatedAt()
        );
    }
}
