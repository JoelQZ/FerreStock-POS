package pe.ferrestock.pos.exception;

import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MENSAJE_TOKEN_INVALIDO = "Token JWT inválido o expirado";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException exception) {
        Map<String, String> errores = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errores.putIfAbsent(error.getField(), mensajeValidacion(error.getDefaultMessage()))
        );
        return ResponseEntity.badRequest().body(errores);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String>> manejarBinding(BindException exception) {
        Map<String, String> errores = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errores.putIfAbsent(error.getField(), mensajeValidacion(error.getDefaultMessage()))
        );
        return ResponseEntity.badRequest().body(errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> manejarConstraintViolation(
            ConstraintViolationException exception
    ) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String ruta = violation.getPropertyPath().toString();
            String campo = ruta.substring(ruta.lastIndexOf('.') + 1);
            errores.putIfAbsent(campo, mensajeValidacion(violation.getMessage()));
        }
        return ResponseEntity.badRequest().body(errores);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, String>> manejarTokenInvalido(JwtException exception) {
        return respuesta(HttpStatus.UNAUTHORIZED, MENSAJE_TOKEN_INVALIDO);
    }

    @ExceptionHandler({ResourceNotFoundException.class, NoSuchElementException.class, NoResourceFoundException.class})
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(Exception exception) {
        return respuesta(HttpStatus.NOT_FOUND, mensaje(exception, "Recurso no encontrado"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErrorGeneral(Exception exception) {
        return respuesta(HttpStatus.BAD_REQUEST, mensaje(exception, "Solicitud inválida"));
    }

    private ResponseEntity<Map<String, String>> respuesta(HttpStatus status, String detail) {
        return ResponseEntity.status(status).body(Map.of("detail", detail));
    }

    private String mensaje(Exception exception, String predeterminado) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? predeterminado : message;
    }

    private String mensajeValidacion(String message) {
        return message == null || message.isBlank() ? "Valor inválido" : message;
    }
}
