package br.com.gabwsv.secure_todo.repository;

import br.com.gabwsv.secure_todo.model.Task;
import br.com.gabwsv.secure_todo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByUser(User user);
}
