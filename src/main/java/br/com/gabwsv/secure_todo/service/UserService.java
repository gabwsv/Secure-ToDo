package br.com.gabwsv.secure_todo.service;

import br.com.gabwsv.secure_todo.dto.user.UserRegistrationDTO;
import br.com.gabwsv.secure_todo.dto.user.UserUpdateDTO;
import br.com.gabwsv.secure_todo.model.User;
import br.com.gabwsv.secure_todo.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User update(String currentUsername, @Valid UserUpdateDTO dto) {
        // 1. Busca o utilizador atual pelo username vindo do Token
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> {
                    log.error("Tentativa de atualizar utilizador inexistente: {}", currentUsername);
                    return new RuntimeException("Usuário não encontrado.");
                });

        // Atualização dos campos permitidos (API03:2023)
        user.setUsername(dto.username());

        log.info("Perfil do usuário {} atualizado com sucesso.", currentUsername);
        return userRepository.save(user);
    }
}
