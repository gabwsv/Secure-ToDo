package br.com.gabwsv.secure_todo.config;

import br.com.gabwsv.secure_todo.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
//@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository repository;

    public ApplicationConfig(UserRepository repository){
        this.repository = repository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    //VULNERABLE [API10:2023]: Unsafe Consumption of APIs
    //Podendo causar negação de serviço ou lentidão, por conta de estar sem timeouts
    public RestClient externalApiClient(RestClient.Builder builder) {
        // 1. Criamos o HttpClient nativo do Java com o Connect Timeout
        HttpClient httpClient = HttpClient.newBuilder()
                .build();

        // 2. Passamos o httpClient pelo CONSTRUTOR da factory
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);

        return builder
                .baseUrl("https://api.externa.com")
                .requestFactory(factory)
                .build();
    }

}
