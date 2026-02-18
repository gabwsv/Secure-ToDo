package br.com.gabwsv.secure_todo.exception;

import br.com.gabwsv.secure_todo.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. Erros de validação (Apenas reporta o que o usuário enviou de errado)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request){
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()){
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de Validação", "Verifique os campos informados", request, errors);
    }

    // 2. Erros de Segurança (BOLA, IDOR, Permissões)
    @ExceptionHandler({AccessDeniedException.class, SecurityException.class})
    public ResponseEntity<ErrorResponse> handleSecurityErrors(Exception ex, HttpServletRequest request){
        //Log do erro real para auditoria interna
        log.warn("Tentativa de violação de segurança no path {}: {}", request.getRequestURI(), ex.getMessage());
        //Retorno de mensagem genérica
        return buildResponse(HttpStatus.FORBIDDEN, "Acesso Negado", ex.getMessage(), request, null);
    }

    // 3. Falha de Autenticação
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleAuthErrors(BadCredentialsException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.UNAUTHORIZED, "Falha na autenticação", "Usuário ou senha inválidos", request, null);
    }

    // 4. Erro genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex, HttpServletRequest request){
        log.error("Erro inesperado no path {}:", request.getRequestURI(), ex);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro Interno", "Ocorreu um erro inesperado. Contate o suporte.", request, null);
    }

    // 5. Erro de JSON/Enum, não mostrar erro técnico para não expor a estrutura interna do código
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ErrorResponse> handleJsonErrors(HttpMessageNotReadableException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.BAD_REQUEST, "JSON Malformado", "Corpo de requisição inválido ou valor de Enum incorreto", request, null);
    }

    // Metódo para montar o JSON
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message, HttpServletRequest request, Map<String, String> validationErrors){
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                request.getRequestURI(),
                validationErrors
        );
        return ResponseEntity.status(status).body(response);
    }
}
