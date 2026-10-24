package cl.duoc.dsy1107.rabbitadmin.exception;

import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.core.NestedExceptionUtils;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones a códigos HTTP honestos. A diferencia del tutorial,
 * un error de RabbitMQ nunca se responde como "creada exitosamente".
 */
@RestControllerAdvice
public class ManejadorDeErrores {

    public record ErrorHttp(String error, String detalle, Instant momento) {
    }

    public record Cuerpo400(String error, Map<String, String> campos, Instant momento) {
    }

    /** La cola no existe: 404. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorHttp> noEncontrado(RecursoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorHttp("No encontrado", e.getMessage(), Instant.now()));
    }

    /** El cuerpo no pasó las validaciones de los DTO: 400, campo por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Cuerpo400> invalido(MethodArgumentNotValidException e) {
        Map<String, String> campos = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        f -> f.getDefaultMessage() == null ? "valor inválido" : f.getDefaultMessage(),
                        (primero, segundo) -> primero));
        return ResponseEntity.badRequest()
                .body(new Cuerpo400("El cuerpo de la petición no es válido", campos, Instant.now()));
    }

    /** No hay conexión con el cluster: 503. */
    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<ErrorHttp> sinConexion(AmqpConnectException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorHttp("RabbitMQ no está disponible", e.getMessage(), Instant.now()));
    }

    /** RabbitMQ rechazó la operación (por ejemplo, la cola ya existe distinta): 409. */
    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<ErrorHttp> rechazadoPorRabbit(AmqpException e) {
        // El mensaje útil suele estar en la causa más profunda, no en la excepción de arriba.
        String detalle = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
        if (detalle == null) {
            detalle = e.getMessage();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorHttp("RabbitMQ rechazó la operación", detalle, Instant.now()));
    }
}