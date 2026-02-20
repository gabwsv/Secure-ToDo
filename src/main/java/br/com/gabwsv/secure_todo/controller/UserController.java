package br.com.gabwsv.secure_todo.controller;

import br.com.gabwsv.secure_todo.dto.user.UserResponseDTO;
import br.com.gabwsv.secure_todo.dto.user.UserUpdateDTO;
import br.com.gabwsv.secure_todo.model.User;
import br.com.gabwsv.secure_todo.repository.UserRepository;
import br.com.gabwsv.secure_todo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "5. Perfil", description = "Atualização de Perfil")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    //VULNERABLE: [API03:2023] Broken Object Property Level Authorization.
    @PutMapping("/profile")
    @Operation(summary = "Atualizar perfil", description = "Permite ao utilizador alterar o username. Impede alteração de privilégios.")
    public ResponseEntity<UserResponseDTO> updateProfile(@RequestBody @Valid User userUpdates, Authentication auth) {
        User currentUser = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        BeanUtils.copyProperties(userUpdates, currentUser, "id");
        return ResponseEntity.ok(new UserResponseDTO(userRepository.save(currentUser)));
    }
}
