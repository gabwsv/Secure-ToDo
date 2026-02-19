package br.com.gabwsv.secure_todo.config;

import br.com.gabwsv.secure_todo.security.JwtAuthenticationFilter;
import br.com.gabwsv.secure_todo.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
                .frameOptions(frame -> frame.deny()) // Previne Clickjacking
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
        );

        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth ->
                                auth.requestMatchers("/auth/**").permitAll()
                                    // CORREÇÃO: Restringe explicitamente o prefixo /admin para a Role ADMIN
                                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/index.html").permitAll()
                                    .requestMatchers("/favicon.ico").permitAll()
                                    .anyRequest().authenticated()
            ).sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
             .authenticationProvider(authenticationProvider)
             .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
             .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


}
