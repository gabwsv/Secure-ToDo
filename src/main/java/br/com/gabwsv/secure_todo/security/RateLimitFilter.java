package br.com.gabwsv.secure_todo.security;

import br.com.gabwsv.secure_todo.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().equals("/auth/login") && request.getMethod().equals("POST")){
            String clientIp = request.getRemoteAddr();
            Bucket tokenBucket = rateLimiterService.resolveBucket(clientIp);

            if(!tokenBucket.tryConsume(1)){
                response.setStatus(429);
                response.getWriter().write("Muitas tentativas de login. Tente novamente em 1 minuto.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
