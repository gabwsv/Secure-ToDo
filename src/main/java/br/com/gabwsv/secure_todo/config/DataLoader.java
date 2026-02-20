package br.com.gabwsv.secure_todo.config;

import br.com.gabwsv.secure_todo.enums.UserRole;
import br.com.gabwsv.secure_todo.model.Task;
import br.com.gabwsv.secure_todo.model.User;
import br.com.gabwsv.secure_todo.repository.TaskRepository;
import br.com.gabwsv.secure_todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public  void run(String... args) throws Exception {
        //limpa as tabelas antes de inserir
        taskRepository.deleteAll();
        userRepository.deleteAll();

        //1. Criar usuários
        User userA = User.builder()
                .username("admin")
                .email("admin@mail.com")
                .password(passwordEncoder.encode("P@ssword123"))
                .role(UserRole.ADMIN)
                .build();

        User userB = User.builder()
                .username("john")
                .email("john@mail.com")
                .password(passwordEncoder.encode("P@ssword123"))
                .role(UserRole.USER)
                .build();

        userRepository.saveAll(List.of(userA, userB));

        //2. Criar Tasks
        for(int i = 1; i <= 3; i++){
            taskRepository.save(Task.builder()
                    .title("Task Admin "+i)
                    .description("Descrição da Task admin "+i)
                    .user(userA)
                    .build());
        }

        for(int i = 1; i <= 3; i++){
            taskRepository.save(Task.builder()
                    .title("Task John "+i)
                    .description("Descrição Tasks John "+i)
                    .user(userB)
                    .build());
        }

        System.out.println("--- DADOS DE TESTE CARREGADOS COM SUCESSO ---");
    }
}
