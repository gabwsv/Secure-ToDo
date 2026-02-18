package br.com.gabwsv.secure_todo.controller;

import br.com.gabwsv.secure_todo.dto.user.UserResponseDTO;
import br.com.gabwsv.secure_todo.dto.user.UserUpdateDTO;
import br.com.gabwsv.secure_todo.model.User;
import br.com.gabwsv.secure_todo.repository.UserRepository;
import br.com.gabwsv.secure_todo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @PutMapping("/profile")
    @Operation(summary = "Atualizar perfil", description = "Permite ao utilizador alterar o username. Impede alteração de privilégios.")
    public ResponseEntity<UserResponseDTO> updateProfile(@RequestBody @Valid UserUpdateDTO dto, Authentication auth) {
        // auth.getName() contém o username extraído do JWT pelo filtro de segurança
        User user = userService.update(auth.getName(), dto);
        return ResponseEntity.ok(new UserResponseDTO(user));
    }
}
