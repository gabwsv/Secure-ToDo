package br.com.gabwsv.secure_todo.config;

import br.com.gabwsv.secure_todo.enums.TaskPriority;
import br.com.gabwsv.secure_todo.enums.TaskStatus;
import br.com.gabwsv.secure_todo.enums.UserRole;
import br.com.gabwsv.secure_todo.model.Task;
import br.com.gabwsv.secure_todo.model.User;
import br.com.gabwsv.secure_todo.repository.TaskRepository;
import br.com.gabwsv.secure_todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0){
            System.out.println("---- BANCO JÁ POPULADO, PULANDO SEED ---");
            return;
        }

        System.out.println("--- INICIANDO POPULAÇÃO DO BANCO DE DADOS ---");

        // 1. Criar Usuários

        String rawPassword = "Pass@123";

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.ADMIN)
                .build();

        User victim = User.builder()
                .username("carlos")
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.USER)
                .build();

        User attacker = User.builder()
                .username("hacker")
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.USER)
                .build();

        userRepository.saveAll(List.of(admin, victim, attacker));

        // 2. Criar Tarefas (Cenários para a Demo)

        // Tarefas da VITIMA (Para serem roubadas via IDOR ou vazadas no SQL Injection)
        Task task1 = Task.builder()
                .title("Recuperar senha do e-mail")
                .description("Minha senha antiga era 'gatinho123', preciso trocar.")
                .priority(TaskPriority.ALTA)
                .status(TaskStatus.PENDENTE)
                .user(victim)
                .build();

        Task task2 = Task.builder()
                .title("Relatório Financeiro Confidencial")
                .description("O lucro da empresa caiu 30%. Não divulgar.")
                .priority(TaskPriority.ALTA)
                .status(TaskStatus.CONCLUIDO)
                .user(victim)
                .build();

        // Tarefas do ADMIN (Para mostrar vazamento de dados críticos)
        Task taskAdmin = Task.builder()
                .title("Rodar Backup do Servidor")
                .description("As chaves de API da AWS estão em /home/admin/keys.txt")
                .priority(TaskPriority.ALTA)
                .status(TaskStatus.PENDENTE)
                .user(admin)
                .build();

        // Tarefa do ATACANTE (Para provar que ele só deveria ver isso)
        Task taskAttacker = Task.builder()
                .title("Comprar leite")
                .description("Tarefa inofensiva do usuário comum.")
                .priority(TaskPriority.BAIXA)
                .status(TaskStatus.PENDENTE)
                .user(attacker)
                .build();

        taskRepository.saveAll(List.of(task1, task2, taskAdmin, taskAttacker));

        System.out.println("--- BANCO POPULADO COM SUCESSO! ---");
        System.out.println("Users criados: admin, usuario_vitima, usuario_atacante");
        System.out.println("Senha para todos: " + rawPassword);

    }

}
