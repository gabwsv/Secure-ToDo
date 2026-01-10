package br.com.gabwsv.secure_todo.service;

import br.com.gabwsv.secure_todo.dto.task.TaskResponseDTO;
import br.com.gabwsv.secure_todo.model.Task;
import br.com.gabwsv.secure_todo.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    @PersistenceContext
    private EntityManager entityManager;

    private final TaskService taskService;

    public List<TaskResponseDTO> searchTaskVulnerable(String termoBusca){
        User user = taskService.getLoggedUser();

        String sql = "SELECT * FROM tb_tasks WHERE user_id = '" + user.getId() + "'" +
                " AND (title LIKE '%" + termoBusca + "%' OR description LIKE '%" + termoBusca + "%')";

        Query query = entityManager.createNativeQuery(sql, Task.class);
        List<Task> tasks = query.getResultList();
        return tasks.stream()
                .map(TaskResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

}
