package br.com.gabwsv.secure_todo.controller;

import br.com.gabwsv.secure_todo.dto.auth.AuthResponse;
import br.com.gabwsv.secure_todo.dto.auth.LoginRequest;
import br.com.gabwsv.secure_todo.dto.auth.RegisterRequest;
import br.com.gabwsv.secure_todo.dto.auth.ResetPasswordRequest;
import br.com.gabwsv.secure_todo.dto.user.UserRegistrationDTO;
import br.com.gabwsv.secure_todo.service.AuthService;
import br.com.gabwsv.secure_todo.service.CaptchaService;
import br.com.gabwsv.secure_todo.service.LockoutService;
import br.com.gabwsv.secure_todo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.channels.FileLock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "1. Autenticação", description = "Endpoints públicos para acesso ao sistema")
public class AuthController {

    private final AuthService service;
    private final LockoutService lockoutService;
    private CaptchaService captchaService;
    private UserService userService;

    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria usuário e retrona o Token JWT. A senha exige: Maiúscula, minúsculas, número e caractere espceial."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro de validação (Senha fraca ou dados inválidos)")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @Operation(
            summary = "Login (Com Rate Limit)",
            description = "Retorna o Token JWT. Protegido por Rate Limit (max 5 tentativas por minuto) contra Brute Force."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login com sucesso"),
            @ApiResponse(responseCode = "403", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas  - IP bloqueado temporariamente"),
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary ="Esqueci minha senha", description = "Envia código para o reset de senha.")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid String email){
        return ResponseEntity.ok(service.generateResetCode(email));
    }

    //VULNERABLE: [API02:2023] Broken Authentication - Quebra de autenticação.
    @PostMapping("/reset-password")
    @Operation(summary = "Reset de senha", description = "Valida código e altera senha com proteção de Lockout")
    public ResponseEntity<?> reset(@RequestBody @Valid ResetPasswordRequest request) {
        if (!service.verifyAndInvalidateCode(request.email(), request.code())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Código inválido.");
        }

        service.updatePassword(request.email(), request.newPassword());

        return ResponseEntity.ok("Senha alterada com sucesso.");
    }
}
