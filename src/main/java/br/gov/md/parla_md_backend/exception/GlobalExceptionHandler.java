package br.gov.md.parla_md_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globalExceptionHandler(Exception ex, WebRequest request) {
        ErroDetalhe erroDetalhe = new ErroDetalhe(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(erroDetalhe, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> runtimeExceptionHandler(RuntimeException ex, WebRequest request) {
        ErroDetalhe erroDetalhe = new ErroDetalhe(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(erroDetalhe, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<RespostaErro> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex,
            org.springframework.web.context.request.WebRequest request) {

        RespostaErro erro = new RespostaErro(
                org.springframework.http.HttpStatus.FORBIDDEN.value(), // status (int)
                java.time.LocalDateTime.now(),                        // timestamp
                "Acesso Negado: " + ex.getMessage(),                  // message
                request.getDescription(false)                         // path
        );

        return new ResponseEntity<>(erro, org.springframework.http.HttpStatus.FORBIDDEN);
    }
}


