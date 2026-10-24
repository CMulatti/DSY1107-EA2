package cl.duoc.dsy1107.rabbitadmin.controller;

import cl.duoc.dsy1107.rabbitadmin.dto.BindingRequest;
import cl.duoc.dsy1107.rabbitadmin.dto.ExchangeRequest;
import cl.duoc.dsy1107.rabbitadmin.dto.QueueInfoResponse;
import cl.duoc.dsy1107.rabbitadmin.dto.QueueRequest;
import cl.duoc.dsy1107.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API REST del administrador de RabbitMQ. Solo recibe peticiones y llama al
 * servicio: aquí no hay lógica de RabbitMQ.
 *
 *   POST   /queues          crea una cola
 *   GET    /queues/{name}   consulta una cola
 *   DELETE /queues/{name}   borra una cola
 *   POST   /exchanges       crea un exchange
 *   DELETE /exchanges/{name} borra un exchange
 *   POST   /bindings        crea un binding
 *   DELETE /bindings        borra un binding
 */
@RestController
public class RabbitAdminController {

    private final RabbitAdminService servicio;

    public RabbitAdminController(RabbitAdminService servicio) {
        this.servicio = servicio;
    }

    // ===== COLAS =====

    @PostMapping("/queues")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> crearCola(@Valid @RequestBody QueueRequest req) {
        servicio.crearCola(req);
        return Map.of("mensaje", "Cola '" + req.name() + "' declarada");
    }

    @GetMapping("/queues/{name}")
    public QueueInfoResponse obtenerCola(@PathVariable String name) {
        return servicio.obtenerCola(name);
    }

    @DeleteMapping("/queues/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCola(@PathVariable String name) {
        servicio.eliminarCola(name);
    }

    // ===== EXCHANGES =====

    @PostMapping("/exchanges")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> crearExchange(@Valid @RequestBody ExchangeRequest req) {
        servicio.crearExchange(req);
        return Map.of("mensaje", "Exchange '" + req.name() + "' declarado");
    }

    @DeleteMapping("/exchanges/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarExchange(@PathVariable String name) {
        servicio.eliminarExchange(name);
    }

    // ===== BINDINGS =====

    @PostMapping("/bindings")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> crearBinding(@Valid @RequestBody BindingRequest req) {
        servicio.crearBinding(req);
        return Map.of("mensaje", "Binding " + req.queue() + " <- " + req.exchange() + " creado");
    }

    @DeleteMapping("/bindings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarBinding(@Valid @RequestBody BindingRequest req) {
        servicio.eliminarBinding(req);
    }
}