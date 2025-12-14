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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request){
        User user = User.builder().username(request.username())
                                 .password(passwordEncoder.encode(request.password()))
                                 .role(request.role())
                                 .build();
        repository.save(user);

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse authenticate(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = repository.findByUsername(request.username()).orElseThrow();

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}
