package br.com.gabwsv.secure_todo.service;

import br.com.gabwsv.secure_todo.dto.auth.AuthResponse;
import br.com.gabwsv.secure_todo.dto.auth.LoginRequest;
import br.com.gabwsv.secure_todo.dto.auth.RegisterRequest;
import br.com.gabwsv.secure_todo.model.User;
import br.com.gabwsv.secure_todo.repository.UserRepository;
import br.com.gabwsv.secure_todo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final Map<String, String> resetTokens = new java.util.concurrent.ConcurrentHashMap<>();

    public AuthResponse register(RegisterRequest request){
        User user = User.builder().username(request.username())
                                 .password(passwordEncoder.encode(request.password()))
                                 .role(request.role())
                                 .build();
        repository.save(user);

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = repository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Username ou Password incorreto"));

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    public String generateResetCode(String email){
        repository.findByEmail(email).ifPresent(user -> {
            // VULNERABLE [API02:2023]: Redução de entropia para 4 digitos
            String code = String.valueOf((int)((Math.random() * 9000) + 1000)); // 6 digitos
            resetTokens.put(email, code);
            //emailService.sendResetCode(email, code); // Chamar o service de email
            System.out.println("DEBUG: Código de Reset para" +email+ " é " +code);
        });

        return "Código enviado.";
    }

    public boolean verifyAndInvalidateCode(String email, Object code) {
        String validCode = resetTokens.get(email);
        if(validCode != null && validCode.equals(code)){
            resetTokens.remove(email); // Invalida após o uso.
            return true;
        }
        return false;
    }

    public void updatePassword(String email, String newPassword) {
      repository.findByEmail(email).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            repository.save(user);
        });
    }
}
