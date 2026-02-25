package br.com.gabwsv.secure_todo.controller;

import br.com.gabwsv.secure_todo.dto.user.UserResponseDTO;
import br.com.gabwsv.secure_todo.dto.user.UserUpdateDTO;
import br.com.gabwsv.secure_todo.model.User;
import br.com.gabwsv.secure_todo.repository.UserRepository;
import br.com.gabwsv.secure_todo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "5. Perfil", description = "Atualização de Perfil")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    //VULNERABLE: [API03:2023] Broken Object Property Level Authorization.
    @PutMapping("/profile")
    @Operation(summary = "Atualizar perfil",
            description = "Permite ao utilizador alterar o username.")
    public ResponseEntity<UserResponseDTO> updateProfile(@RequestBody Map<String, Object> updates, Authentication auth) {
        // Busca o usuário atual
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        //Utiliza Jackson para mapear as chaves do JSON diretamente para os atributos da classe User
        ObjectMapper mapper = new ObjectMapper();
        User userUpdates = mapper.convertValue(updates, User.class);

        BeanUtils.copyProperties(userUpdates, currentUser, getNullPropertyNames(userUpdates));
        return ResponseEntity.ok(new UserResponseDTO(userRepository.save(currentUser)));
    }

    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        return emptyNames.toArray(new String[0]);
    }
}
